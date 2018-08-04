package mwittmann.neo4japp.parse

import org.neo4j.driver.v1.Record
import org.neo4j.driver.v1.types.Node


trait N4j

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

  override def getNodes(name: String): List[WrappedNode] = record.get(name).asList(_.asNode()).asScala.map(WrappedNodeImpl)

  override def getAtom(name: String): WrappedAtom = WrappedAtomImpl(record.get(name))

  override def getAtoms(name: String): List[WrappedAtom] = record.get(name).asList(i => i).asScala.map(WrappedAtomImpl)

  override def getMolecule(name: String): WrappedMolecule = WrappedMoleculeImpl(record.get(name))

  override def getMolecules(name: String): List[WrappedMolecule] = record.get(name).asList(i => i).asScala.map(WrappedMoleculeImpl)
}

sealed trait WrappedNode extends N4j {
  def getAtom(name: String): WrappedAtom
  def getAtoms(name: String): List[WrappedAtom]
}

case class WrappedNodeImpl(node: Node) extends WrappedNode {
  override def getAtom(name: String): WrappedAtom = WrappedAtomImpl(node.get(name))

  override def getAtoms(name: String): List[WrappedAtom] = node.get(name).asList(v => v).asScala.map(WrappedAtomImpl)
}

sealed trait WrappedAtom extends N4j {
  def asLong: Long
  def asString: String
  // asFoo: Foo
}



sealed trait WrappedMolecule extends N4j {
  def asNode: WrappedNode
  def asNodes: List[WrappedNode]

  def asAtom: WrappedAtom
  def asAtoms: List[WrappedAtom]

  def asMolecule: WrappedMolecule
  def asMolecules: List[WrappedMolecule]
}



object ParseNeo4j {

}
