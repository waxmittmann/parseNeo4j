package mwittmann.neo4japp.parsewitherror

import scala.collection.JavaConverters._

import org.neo4j.driver.v1.Record

import mwittmann.neo4japp.parsewitherror.ParseN4j.{Result, tryCatch}

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
      record.get(name).asList(i => i).asScala.map(WrappedAtomImpl.apply).toList,
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
}
