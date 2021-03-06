//package mwittmann.neo4japp.parsewitherror
//
//import scala.collection.JavaConverters._
//
//import mwittmann.neo4japp.parsewitherror.ParseN4j.{Result, tryCatch}
//import org.neo4j.driver.v1.Value
//
//
//
////object WrappedMoleculeImpl {
////
////  object Implicits {
////
////  }
////
////}
//
//case class WrappedMoleculeImpl(value: Value) extends WrappedMolecule {
//  override def asNode: Result[WrappedNode] =
//    tryCatch(
//      WrappedNodeImpl(value.asNode()),
//      s"$value not a ???"
//    )
//
//  override def asNodes: Result[List[WrappedNode]] =
//    tryCatch(
//      value.asList(_.asNode()).asScala.toList.map(WrappedNodeImpl.apply),
//      s"$value not a ???"
//    )
//
//  override def asAtom: Result[WrappedAtom] =
//    tryCatch(
//      WrappedAtomImpl(value),
//      s"$value not a ???"
//    )
//
//  override def asAtoms: Result[List[WrappedAtom]] =
//    tryCatch(
//      value.asList(v => v).asScala.toList.map(WrappedAtomImpl.apply),
//      s"$value not a ???"
//    )
//
//  override def asMolecules: Result[List[WrappedMolecule]] =
//    tryCatch(
//      value.asList(v => v).asScala.toList.map(WrappedMoleculeImpl),
//      s"$value not a ???"
//    )
//
//  override def nonNull: Boolean = !value.isNull
//
//  override def asMolecule: Result[WrappedMolecule] =
//    tryCatch(
//      value.asList(v => v).asScala.toList.map(WrappedMoleculeImpl).head,
//      s"$value not a ???"
//    )
//}
