package mwittmann.neo4japp.parse

import java.util.UUID

import org.neo4j.driver.v1.{Record, Value}
import org.neo4j.driver.v1.types.Node
import scala.collection.JavaConverters._
import scala.collection.immutable

import mwittmann.neo4japp.parsewitherror


sealed trait N4j
//sealed trait NotARecord extends N4j



sealed trait WrappedRecord extends parsewitherror.N4j {
  def getNode(name: String): parsewitherror.WrappedNode
  def getNodes(name: String): List[parsewitherror.WrappedNode]

  def getAtom(name: String): parsewitherror.WrappedAtom
  def getAtoms(name: String): List[parsewitherror.WrappedAtom]

  def getMolecule(name: String): parsewitherror.WrappedMolecule
  def getMolecules(name: String): List[parsewitherror.WrappedMolecule]
}

case class WrappedRecordImpl(record: Record) extends parsewitherror.WrappedRecord {
  override def getNode(name: String): parsewitherror.WrappedNode = parsewitherror.WrappedNodeImpl(record.get(name).asNode())
  override def getNodes(name: String): List[parsewitherror.WrappedNode] =
    record.get(name).asList(_.asNode()).asScala.map(WrappedNodeImpl).toList

  override def getAtom(name: String): parsewitherror.WrappedAtom = parsewitherror.WrappedAtomImpl(record.get(name))
  override def getAtoms(name: String): List[parsewitherror.WrappedAtom] =
    record.get(name).asList(i => i).asScala.map(WrappedAtomImpl).toList

  override def getMolecule(name: String): parsewitherror.WrappedMolecule = parsewitherror.WrappedMoleculeImpl(record.get(name))
  override def getMolecules(name: String): List[parsewitherror.WrappedMolecule] =
    record.get(name).asList(i => i).asScala.map(WrappedMoleculeImpl).toList
}

sealed trait WrappedNode extends parsewitherror.N4j {
  def getAtom(name: String): parsewitherror.WrappedAtom
  def getAtoms(name: String): List[parsewitherror.WrappedAtom]
}

case class WrappedNodeImpl(node: Node) extends parsewitherror.WrappedNode {
  override def getAtom(name: String): parsewitherror.WrappedAtom = parsewitherror.WrappedAtomImpl(node.get(name))
  override def getAtoms(name: String): List[parsewitherror.WrappedAtom] =
    node.get(name).asList(v => v).asScala.map(WrappedAtomImpl).toList
}

sealed trait WrappedMolecule extends parsewitherror.N4j {
  def nonNull: Boolean

  def asNode: parsewitherror.WrappedNode
  def asNodes: List[parsewitherror.WrappedNode]

  def asAtom: parsewitherror.WrappedAtom
  def asAtoms: List[parsewitherror.WrappedAtom]

  def asMolecules: List[parsewitherror.WrappedMolecule]
}

case class WrappedMoleculeImpl(value: Value) extends parsewitherror.WrappedMolecule {
  override def asNode: parsewitherror.WrappedNode = parsewitherror.WrappedNodeImpl(value.asNode())
  override def asNodes: List[parsewitherror.WrappedNode] = value.asList(_.asNode()).asScala.toList.map(WrappedNodeImpl)

  override def asAtom: parsewitherror.WrappedAtom = parsewitherror.WrappedAtomImpl(value)
  override def asAtoms: List[parsewitherror.WrappedAtom] = value.asList(v => v).asScala.toList.map(WrappedAtomImpl)

  override def asMolecules: List[parsewitherror.WrappedMolecule] = value.asList(v => v).asScala.toList.map(WrappedMoleculeImpl)

  override def nonNull: Boolean = !value.isNull
}

sealed trait WrappedAtom extends parsewitherror.N4j {
  def asUid: UUID = UUID.fromString(asString)
  def asLong: Long
  def asString: String
  // asFoo: Foo
}

case class WrappedAtomImpl(value: Value) extends parsewitherror.WrappedAtom {
  override def asLong: Long = value.asLong()

  override def asString: String = value.asString()
}


object ParseNeo4j {

  def main(args: List[String]): Unit = {

    val result: parsewitherror.WrappedRecord = ???

    val workflowInstanceNode: parsewitherror.WrappedNode = result.getNode("workflowInstance")

    val inputMolecules: List[parsewitherror.WrappedMolecule] = result.getMolecules("inputs")
    val inputParts: List[(parsewitherror.WrappedNode, parsewitherror.WrappedNode, parsewitherror.WrappedNode)] = inputMolecules.map { molecule =>
      val inputParts = molecule.asMolecules
      val artifactDefnNode = inputParts(0).asNode
      val artifactNode = inputParts(1).asNode
      val fileDataNode = inputParts(2).asNode

      (artifactDefnNode, artifactNode, fileDataNode)
    }
  }

  type NodeParser[S] = parsewitherror.WrappedNode => S
  type AtomParser[S] = parsewitherror.WrappedAtom => S
  type MoleculeParser[S] = parsewitherror.WrappedMolecule => S
  type RecordParser[S] = parsewitherror.WrappedRecord => S

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
