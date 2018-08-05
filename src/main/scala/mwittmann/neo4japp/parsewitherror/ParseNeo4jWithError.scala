package mwittmann.neo4japp.parsewitherror

import java.util.UUID
import scala.collection.JavaConverters._

import cats.syntax._
import cats.implicits._

import mwittmann.neo4japp.parsewitherror.N4j._

object N4j {
  // Result type for parse
  type Result[S] = Either[(String, Option[Exception]), S]

  // Parsers for the different wrapped neo4j objects
  type NodeParser[S] = WrappedNode => Result[S]
  type AtomParser[S] = WrappedAtom => Result[S]
  type MoleculeParser[S] = WrappedMolecule => Result[S]
  type RecordParser[S] = WrappedRecord => Result[S]

  // Util for implementations to catch neo4j conversion errors
  def tryCatch[S](fn: => S, error: String): Result[S] =
    try {
      Right(fn)
    } catch {
      case e: Exception => Left((error, Some(e)))
    }

  // Convert a node parser to a molecule parser
  implicit def moleculeFromNode[S](np: NodeParser[S]): MoleculeParser[S] = { molecule =>
    for {
      n <- molecule.asNode
      n2 <- np(n)
    } yield n2
  }

  // Optionally match what the input parser matches
  def optional[S](s: MoleculeParser[S]): MoleculeParser[Option[S]] = { molecule =>
    if (molecule.nonNull)
      s(molecule).map((v: S) => Some(v))
    else
      Right(None)
  }

  // Combine two molecule parsers
  def two[S, T](
    s: MoleculeParser[S],
    t: MoleculeParser[T]
  ): MoleculeParser[(S, T)] = { molecule =>
    for {
      items <- molecule.asMolecules
      si <- s(items(0))
      ti <- t(items(1))
    } yield (si, ti)
  }

  // Combine three molecule parsers
  def three[S, T, U](
    s: MoleculeParser[S],
    t: MoleculeParser[T],
    u: MoleculeParser[U]
  ): MoleculeParser[(S, T, U)] = { molecule =>
    for {
      items <- molecule.asMolecules
      si <- s(items(0))
      ti <- t(items(1))
      ui <- u(items(2))
    } yield (si, ti, ui)
  }
}

sealed trait N4j

// A wrapped Record, which may contain WrappedNode(s), WrappedMolecule(s) or WrappedAtom(s)
trait WrappedRecord extends N4j {
  def getNode(name: String): Result[WrappedNode]
  def getNodes(name: String): Result[List[WrappedNode]]

  def getAtom(name: String): Result[WrappedAtom]
  def getAtoms(name: String): Result[List[WrappedAtom]]

  def getMolecule(name: String): Result[WrappedMolecule]
  def getMolecules(name: String): Result[List[WrappedMolecule]]
}

// A wrapped Node, which may contain atomic values or lists of atomic values
trait WrappedNode extends N4j {
  def getAtom(name: String): Result[WrappedAtom]
  def getAtoms(name: String): Result[List[WrappedAtom]]
}

// A wrapped Value that can be either a node, an atomic value or a list of Values
trait WrappedMolecule extends N4j {
  def nonNull: Boolean

  def asNode: Result[WrappedNode]
  def asNodes: Result[List[WrappedNode]]

  def asAtom: Result[WrappedAtom]
  def asAtoms: Result[List[WrappedAtom]]

  def asMolecules: Result[List[WrappedMolecule]]
}

// A wrapped Value that can be an atomic value; lists of values come from the wrapped parent via a plural (ends in s)method
trait WrappedAtom extends N4j {
  def asUid: Result[UUID] =
    asString.map(UUID.fromString)

  def asLong: Result[Long]
  def asString: Result[String]
}
