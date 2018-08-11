package mwittmann.neo4japp.experiment

import java.util.UUID

import mwittmann.neo4japp.parsewitherror.ParseN4j.Result
import mwittmann.neo4japp.parsewitherror.{WrappedAtom, WrappedMolecule, WrappedNode, WrappedRecord}
import org.neo4j.driver.internal.value._
import org.neo4j.driver.v1.Value

object FakeN4j {

  case class FakeWrappedRecord(
    node: Map[String, WrappedNode] = Map.empty,
    nodes: Map[String, List[WrappedNode]] = Map.empty,
    atom: Map[String, WrappedAtom] = Map.empty,
    atoms: Map[String, List[WrappedAtom]] = Map.empty,
    molecule: Map[String, WrappedMolecule] = Map.empty,
    molecules: Map[String, List[WrappedMolecule]] = Map.empty
  ) extends WrappedRecord {
    
    override def getNode(name: String): Result[WrappedNode] =
      node.get(name).map(Right.apply).getOrElse(Left(s"No node $name", None))

    override def getNodes(name: String): Result[List[WrappedNode]] = 
      nodes.get(name).map(Right.apply).getOrElse(Left(s"No nodes $name", None))

    override def getAtom(name: String): Result[WrappedAtom] =
      atom.get(name).map(Right.apply).getOrElse(Left(s"No atom $name", None))

    override def getAtoms(name: String): Result[List[WrappedAtom]] =
     atoms.get(name).map(Right.apply).getOrElse(Left(s"No atoms $name", None))

    override def getMolecule(name: String): Result[WrappedMolecule] =
      molecule.get(name).map(Right.apply).getOrElse(Left(s"No molecule $name", None))
    
    override def getMolecules(name: String): Result[List[WrappedMolecule]] =
      molecules.get(name).map(Right.apply).getOrElse(Left(s"No molecules $name", None))
  }
  
  case class FakeWrappedNode(
    atom: Map[String, WrappedAtom] = Map.empty,
    atoms: Map[String, List[WrappedAtom]] = Map.empty
  ) extends WrappedNode {
    override def getAtom(name: String): Result[WrappedAtom] =
      atom.get(name).map(Right.apply).getOrElse(Left(s"No atom $name", None))

    override def getAtoms(name: String): Result[List[WrappedAtom]] =
      atoms.get(name).map(Right.apply).getOrElse(Left(s"No atoms $name", None))
  }

  case class FakeWrappedMolecule(
    node: Option[WrappedNode] = None,
    nodes: Option[List[WrappedNode]]= None,
    atom: Option[WrappedAtom] = None,
    atoms: Option[List[WrappedAtom]] = None,
    molecules: Option[List[WrappedMolecule]] = None
  ) extends WrappedMolecule {
    override def nonNull: Boolean =
      node.isDefined || nodes.isDefined || atom.isDefined || atoms.isDefined || molecules.isDefined

    override def asNode: Result[WrappedNode] =
      node.map(Right.apply).getOrElse(Left("Not a node", None))

    override def asNodes: Result[List[WrappedNode]] =
      nodes.map(Right.apply).getOrElse(Left("Not a node", None))

    override def asAtom: Result[WrappedAtom] =
      atom.map(Right.apply).getOrElse(Left("Not a node", None))

    override def asAtoms: Result[List[WrappedAtom]] =
      atoms.map(Right.apply).getOrElse(Left("Not a node", None))

    override def asMolecules: Result[List[WrappedMolecule]] =
      molecules.map(Right.apply).getOrElse(Left("Not a node", None))
  }
  
//  case class FakeWrappedAtom(
//    strVal: Option[String],
//    uidVal: Option[UUID],
//    longVal: Option[Long],
//    intVal: Option[Int]
//  ) extends WrappedAtom {
//    override def value: Value = {
//      //val ts = TypeSystem
//    }
//  }

  case class FakeWrappedInt(v: Int) extends WrappedAtom {
    override def value: Value = new IntegerValue(v)
  }

  case class FakeWrappedString(v: String) extends WrappedAtom {
    override def value: Value = new StringValue(v)
  }

  case class FakeWrappedUUID(v: UUID) extends WrappedAtom {
    override def value: Value = new StringValue(v.toString)
  }

  case class FakeWrappedLong(v: Long) extends WrappedAtom {
    override def value: Value = new IntegerValue(v)
  }

}
