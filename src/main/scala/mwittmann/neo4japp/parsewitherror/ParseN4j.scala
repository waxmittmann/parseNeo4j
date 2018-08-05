package mwittmann.neo4japp.parsewitherror

import java.util.UUID
import scala.collection.JavaConverters._

import cats.syntax._
import cats.implicits._
import mwittmann.neo4japp.parsewitherror.ParseN4j._
import org.neo4j.driver.v1.{Record, Value}

object ParseN4j {
  // Result type for parse
  type Result[S] = Either[(String, Option[Exception]), S]

  // Parsers for the different wrapped neo4j objects
  type NodeParser[S] = WrappedNode => Result[S]
  type AtomParser[S] = WrappedAtom => Result[S]
  type MoleculeParser[S] = WrappedMolecule => Result[S]
  type RecordParser[S] = WrappedRecord => Result[S]

  implicit class NodeGetDirect(wn: WrappedNode) {
    def get[S](field: String)(implicit atomParser: AtomParser[S]): Result[S] = for {
      atom <- wn.getAtom(field)
      value <- atomParser(atom)
    } yield value
  }

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
      _ <-
        if (items.size < 2) Left("Fewer than 2 items", None)
        else if (items.size > 2) Left(("More than 2 items", None))
        else Right(())

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
      _ <-
        if (items.size < 3) Left("Fewer than 3 items", None)
        else if (items.size > 3) Left(("More than 3 items", None))
        else Right(())

      si <- s(items(0))
      ti <- t(items(1))
      ui <- u(items(2))
    } yield (si, ti, ui)
  }



  def parseRecords[S](alias: String, parser: NodeParser[S]): List[Record] => Result[List[S]] = { (records: List[Record]) =>
    val p: RecordParser[S] = parseNodeList(alias, parser)
    records.map(r => p(WrappedRecordImpl(r))).sequence[Result, S]
  }

  // Make a record parser from node parser
//  def parseNodeList[S](alias: String, parser: NodeParser[S]): RecordParser[List[S]] = { record =>
  def parseNodeList[S](alias: String, parser: NodeParser[S]): RecordParser[S] = { record =>
    for {
//      nodes <- record.getNodes(alias)
      nodes <- record.getNode(alias)
//      result <- nodes.map(parser).sequence[Result, S]
      result <- parser(nodes)
    } yield result
    ///record.getNodes(alias).flatMap().sequence[Result, WrappedNode]
  }
}

sealed trait ParseN4j

// A wrapped Record, which may contain WrappedNode(s), WrappedMolecule(s) or WrappedAtom(s)
trait WrappedRecord extends ParseN4j {
  def getNode(name: String): Result[WrappedNode]
  def getNodes(name: String): Result[List[WrappedNode]]

  def getAtom(name: String): Result[WrappedAtom]
  def getAtoms(name: String): Result[List[WrappedAtom]]

  def getMolecule(name: String): Result[WrappedMolecule]
  def getMolecules(name: String): Result[List[WrappedMolecule]]
}

// A wrapped Node, which may contain atomic values or lists of atomic values
trait WrappedNode extends ParseN4j {
  def getAtom(name: String): Result[WrappedAtom]
  def getAtoms(name: String): Result[List[WrappedAtom]]
}

// A wrapped Value that can be either a node, an atomic value or a list of Values
trait WrappedMolecule extends ParseN4j {
  def nonNull: Boolean

  def asNode: Result[WrappedNode]
  def asNodes: Result[List[WrappedNode]]

  def asAtom: Result[WrappedAtom]
  def asAtoms: Result[List[WrappedAtom]]

  def asMolecules: Result[List[WrappedMolecule]]
}

// A wrapped Value that is an atomic value; lists of values come from the wrapped parent via a plural (ends in s) method
trait WrappedAtom extends ParseN4j {
  def as[S](implicit atomParser: AtomParser[S]): Result[S] = atomParser(this)

  def value: Value
}
