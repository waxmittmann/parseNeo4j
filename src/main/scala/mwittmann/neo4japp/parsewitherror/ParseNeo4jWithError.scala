package mwittmann.neo4japp.parsewitherror

import java.util.UUID
import scala.collection.JavaConverters._
import cats.syntax._
import cats.implicits._

import mwittmann.neo4japp.parsewitherror.N4j._
import org.neo4j.driver.v1.types.Node
import org.neo4j.driver.v1.{Record, Value}

object N4j {
  type Result[S] = Either[(String, Option[Exception]), S]

  def tryCatch[S](fn: => S, error: String): Result[S] =
    try {
      Right(fn)
    } catch {
      case e: Exception => Left((error, Some(e)))
    }
}

sealed trait N4j
//sealed trait NotARecord extends N4j



sealed trait WrappedRecord extends N4j {
  def getNode(name: String): Result[WrappedNode]
  def getNodes(name: String): Result[List[WrappedNode]]

  def getAtom(name: String): Result[WrappedAtom]
  def getAtoms(name: String): Result[List[WrappedAtom]]

  def getMolecule(name: String): Result[WrappedMolecule]
  def getMolecules(name: String): Result[List[WrappedMolecule]]
}

case class WrappedRecordImpl(record: Record) extends WrappedRecord {
  override def getNode(name: String): Result[WrappedNode] =
    tryCatch(WrappedNodeImpl(record.get(name).asNode()), s"${record}.get($name) wasn't a node")

  override def getNodes(name: String): Result[List[WrappedNode]] =
    tryCatch(
      record.get(name).asList(_.asNode()).asScala.map(WrappedNodeImpl).toList,
      s"${record}.get($name) wasn't a node list"
    )

  override def getAtom(name: String): Result[WrappedAtom] =
    tryCatch(
      WrappedAtomImpl(record.get(name)),
      s"${record}.get($name) didn't exist"
    )

  override def getAtoms(name: String): Result[List[WrappedAtom]] =
    tryCatch(
      record.get(name).asList(i => i).asScala.map(WrappedAtomImpl).toList,
      s"${record}.get($name) wasn't an atom list"
    )

  override def getMolecule(name: String): Result[WrappedMolecule] =
    tryCatch(
      WrappedMoleculeImpl(record.get(name)),
      s"${record}.get($name) wasn't a molecule"
    )

  override def getMolecules(name: String): Result[List[WrappedMolecule]] =
    tryCatch(
      record.get(name).asList(i => i).asScala.map(WrappedMoleculeImpl).toList,
      s"${record}.get($name) wasn't a molecule"
    )

  //    record.get(name).asList(i => i).asScala.map(WrappedMoleculeImpl).toList
}

sealed trait WrappedNode extends N4j {
  def getAtom(name: String): Result[WrappedAtom]
  def getAtoms(name: String): Result[List[WrappedAtom]]
}

case class WrappedNodeImpl(node: Node) extends WrappedNode {
  override def getAtom(name: String): Result[WrappedAtom] =
    tryCatch(
      WrappedAtomImpl(node.get(name)),
      s"${node}.get($name) wasn't a molecule"
    )

  override def getAtoms(name: String): Result[List[WrappedAtom]] =
    tryCatch(
      node.get(name).asList(v => v).asScala.map(WrappedAtomImpl).toList,
      s"${node}.get($name) wasn't a molecule"
    )
}

sealed trait WrappedMolecule extends N4j {
  def nonNull: Boolean

  def asNode: Result[WrappedNode]
  def asNodes: Result[List[WrappedNode]]

  def asAtom: Result[WrappedAtom]
  def asAtoms: Result[List[WrappedAtom]]

  def asMolecules: Result[List[WrappedMolecule]]
}

case class WrappedMoleculeImpl(value: Value) extends WrappedMolecule {
  override def asNode: Result[WrappedNode] =
    tryCatch(
      WrappedNodeImpl(value.asNode()),
      s"$value not a ???"
    )

  override def asNodes: Result[List[WrappedNode]] =
    tryCatch(
      value.asList(_.asNode()).asScala.toList.map(WrappedNodeImpl),
      s"$value not a ???"
    )

  override def asAtom: Result[WrappedAtom] =
    tryCatch(
      WrappedAtomImpl(value),
      s"$value not a ???"
    )

  override def asAtoms: Result[List[WrappedAtom]] =
    tryCatch(
      value.asList(v => v).asScala.toList.map(WrappedAtomImpl),
      s"$value not a ???"
    )

  override def asMolecules: Result[List[WrappedMolecule]] =
    tryCatch(
      value.asList(v => v).asScala.toList.map(WrappedMoleculeImpl),
      s"$value not a ???"
    )

  override def nonNull: Boolean = !value.isNull
}

sealed trait WrappedAtom extends N4j {
  def asUid: Result[UUID] =
    asString.map(UUID.fromString)

  def asLong: Result[Long]
  def asString: Result[String]
  // asFoo: Foo
}

case class WrappedAtomImpl(value: Value) extends WrappedAtom {
  override def asLong: Result[Long] =
    tryCatch(
      value.asLong(),
      s"$value not a ???"
    )

  override def asString: Result[String] =
    tryCatch(
      value.asString(),
      s"$value not a ???"
    )
}


object ParseNeo4j {

  def main(args: List[String]): Unit = {

    val result: WrappedRecord = ???

    val workflowInstanceNode: Result[WrappedNode] = result.getNode("workflowInstance")

    val inputParts = for {
      inputMolecules <- result.getMolecules("inputs")

      inputParts <- {
        inputMolecules.map { molecule =>
          for {
            inputParts <- molecule.asMolecules
            artifactDefnNode <- inputParts(0).asNode
            artifactNode <- inputParts(1).asNode
            fileDataNode <- inputParts(2).asNode
          } yield (artifactDefnNode, artifactNode, fileDataNode)
        }.sequence
      }
    } yield inputParts
  }

  type NodeParser[S] = WrappedNode => Result[S]
  type AtomParser[S] = WrappedAtom => Result[S]
  type MoleculeParser[S] = WrappedMolecule => Result[S]
  type RecordParser[S] = WrappedRecord => Result[S]

  //type NARParser[S] = NotARecord => S
  
  case class Artifact(uid: UUID, blobUid: UUID)

  trait ArtifactDefn
  trait FileData
  val artifactDefnParser: NodeParser[Artifact] = { node =>
    for {
      uidAtom <- node.getAtom("uid")
      uid     <-  uidAtom.asUid

      blobUidAtom <- node.getAtom("blobUid")
      blobUid <-  blobUidAtom.asUid
    } yield Artifact(uid, blobUid)
  }

  val artifactParser: NodeParser[ArtifactDefn] = ???

  val fileData: NodeParser[FileData] = ???

  implicit def moleculeFromNode[S](np: NodeParser[S]): MoleculeParser[S] = { molecule =>
    for {
      n <- molecule.asNode
      n2 <- np(n)
    } yield n2
  }

  def optional[S](s: MoleculeParser[S]): MoleculeParser[Option[S]] = { molecule =>
    if (molecule.nonNull)
      s(molecule).map((v: S) => Some(v))
    else
      Right(None)
  }

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

  val inputsParser: MoleculeParser[(Artifact, ArtifactDefn, Option[FileData])] =
    three(artifactDefnParser, artifactParser, optional(fileData))

}
