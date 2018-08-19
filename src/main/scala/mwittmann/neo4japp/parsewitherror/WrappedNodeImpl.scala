//package mwittmann.neo4japp.parsewitherror
//
//import scala.collection.JavaConverters._
//
//import mwittmann.neo4japp.parsewitherror.ParseN4j.{Result, tryCatch}
//import org.neo4j.driver.v1.types.Node
//
//case class WrappedNodeImpl(node: Node) extends WrappedNode {
//  override def getAtom(name: String): Result[WrappedAtom] =
//    tryCatch(
//      WrappedAtomImpl(node.get(name)),
//      s"${node}.get($name) wasn't a molecule"
//    )
//
//  override def getAtoms(name: String): Result[List[WrappedAtom]] =
//    tryCatch(
//      node.get(name).asList(v => v).asScala.map(WrappedAtomImpl.apply).toList,
//      s"${node}.get($name) wasn't a molecule"
//    )
//}
