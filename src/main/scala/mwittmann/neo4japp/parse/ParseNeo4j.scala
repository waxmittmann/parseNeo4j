package mwittmann.neo4japp.parse

import java.util.UUID

import org.neo4j.driver.v1.{Record, Value}
import org.neo4j.driver.v1.types.Node
import scala.collection.JavaConverters._
import scala.collection.immutable

import mwittmann.neo4japp.parsewitherror


sealed trait N4j
//sealed trait NotARecord extends N4j



sealed trait WrappedRecord extends N4j {
  def getNode(name: String): WrappedNode
  def getNodes(name: String): List[WrappedNode]

  def getAtom(name: String): WrappedAtom
  def getAtoms(name: String): List[WrappedAtom]

  def getMolecule(name: String): WrappedMolecule
  def getMolecules(name: String): List[WrappedMolecule]
}

case class WrappedRecordImpl(record: Record) extends WrappedRecord {
  override def getNode(name: String): WrappedNode = WrappedNodeImpl(record.get(name).asNode())
  override def getNodes(name: String): List[WrappedNode] =
    record.get(name).asList(_.asNode()).asScala.map(WrappedNodeImpl).toList

  override def getAtom(name: String): WrappedAtom = WrappedAtomImpl(record.get(name))
  override def getAtoms(name: String): List[WrappedAtom] =
    record.get(name).asList(i => i).asScala.map(WrappedAtomImpl).toList

  override def getMolecule(name: String): WrappedMolecule = WrappedMoleculeImpl(record.get(name))
  override def getMolecules(name: String): List[WrappedMolecule] =
    record.get(name).asList(i => i).asScala.map(WrappedMoleculeImpl).toList
}

sealed trait WrappedNode extends N4j {
  def getAtom(name: String): WrappedAtom
  def getAtoms(name: String): List[WrappedAtom]
}

case class WrappedNodeImpl(node: Node) extends WrappedNode {
  override def getAtom(name: String): WrappedAtom = WrappedAtomImpl(node.get(name))
  override def getAtoms(name: String): List[WrappedAtom] =
    node.get(name).asList(v => v).asScala.map(WrappedAtomImpl).toList
}

sealed trait WrappedMolecule extends N4j {
  def nonNull: Boolean

  def asNode: WrappedNode
  def asNodes: List[WrappedNode]

  def asAtom: WrappedAtom
  def asAtoms: List[WrappedAtom]

  def asMolecules: List[WrappedMolecule]
}

case class WrappedMoleculeImpl(value: Value) extends WrappedMolecule {
  override def asNode: WrappedNode = WrappedNodeImpl(value.asNode())
  override def asNodes: List[WrappedNode] = value.asList(_.asNode()).asScala.toList.map(WrappedNodeImpl)

  override def asAtom: WrappedAtom = WrappedAtomImpl(value)
  override def asAtoms: List[WrappedAtom] = value.asList(v => v).asScala.toList.map(WrappedAtomImpl)

  override def asMolecules: List[WrappedMolecule] = value.asList(v => v).asScala.toList.map(WrappedMoleculeImpl)

  override def nonNull: Boolean = !value.isNull
}

sealed trait WrappedAtom extends N4j {
  def asUid: UUID = UUID.fromString(asString)
  def asLong: Long
  def asString: String
  // asFoo: Foo
}

case class WrappedAtomImpl(value: Value) extends WrappedAtom {
  override def asLong: Long = value.asLong()

  override def asString: String = value.asString()
}


object ParseNeo4j {

  def main(args: List[String]): Unit = {

    val result: WrappedRecord = ???

    val workflowInstanceNode: WrappedNode = result.getNode("workflowInstance")

    val inputMolecules: List[WrappedMolecule] = result.getMolecules("inputs")
    val inputParts: List[(WrappedNode, WrappedNode, WrappedNode)] = inputMolecules.map { molecule =>
      val inputParts = molecule.asMolecules
      val artifactDefnNode = inputParts(0).asNode
      val artifactNode = inputParts(1).asNode
      val fileDataNode = inputParts(2).asNode

      (artifactDefnNode, artifactNode, fileDataNode)
    }
  }

  type NodeParser[S] = WrappedNode => S
  type AtomParser[S] = WrappedAtom => S
  type MoleculeParser[S] = WrappedMolecule => S
  type RecordParser[S] = WrappedRecord => S

  //type NARParser[S] = NotARecord => S


  case class Artifact(uid: UUID, blobUid: UUID)

  trait ArtifactDefn
  trait FileData
  val artifactDefnParser: NodeParser[Artifact] = { node =>
    Artifact(
      node.getAtom("uid").asUid,
      node.getAtom("blobUid").asUid
    )
  }

  val artifactParser: NodeParser[ArtifactDefn] = ???
  val fileData: NodeParser[FileData] = ???

  implicit def moleculeFromNode[S](np: NodeParser[S]): MoleculeParser[S] = { molecule =>
    np(molecule.asNode)
  }

  def optional[S](s: MoleculeParser[S]): MoleculeParser[Option[S]] = { molecule =>
    if (molecule.nonNull)
      Some(s(molecule))
    else
      None
  }


//  def three[S, T, U](s: NodeParser[])
  def three[S, T, U](s: MoleculeParser[S], t: MoleculeParser[T], u: MoleculeParser[U]): MoleculeParser[(S, T, U)] = { molecule =>
    val items = molecule.asMolecules
    (
      s(items(0)),
      t(items(1)),
      u(items(2))
    )
  }

  val inputsParser: MoleculeParser[(Artifact, ArtifactDefn, Option[FileData])] =
    three(artifactDefnParser, artifactParser, optional(fileData))


}
