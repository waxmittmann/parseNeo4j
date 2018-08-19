//package mwittmann.neo4japp.experiment
//
//import java.util.UUID
//
//import mwittmann.neo4japp.experiment.FakeN4j.FakeWrappedMolecule
//import mwittmann.neo4japp.parsewitherror.ParseN4j.Result
//import mwittmann.neo4japp.parsewitherror.{WrappedAtom, WrappedMolecule, WrappedNode, WrappedRecord}
//import org.neo4j.driver.internal.value._
//import org.neo4j.driver.v1.Value
//
//object FakeN4j {
//
//  case class FakeWrappedRecord(
//    node: Map[String, WrappedNode] = Map.empty,
//    nodes: Map[String, List[WrappedNode]] = Map.empty,
//    atom: Map[String, WrappedAtom] = Map.empty,
//    atoms: Map[String, List[WrappedAtom]] = Map.empty,
//    molecule: Map[String, WrappedMolecule] = Map.empty,
//    molecules: Map[String, List[WrappedMolecule]] = Map.empty
//  ) extends WrappedRecord {
//
//    override def getNode(name: String): Result[WrappedNode] =
//      get(node, "node", name)
//
//    override def getNodes(name: String): Result[List[WrappedNode]] =
//      get(nodes, "nodes", name)
//
//    override def getAtom(name: String): Result[WrappedAtom] =
//      get(atom, "atom", name)
//
//    override def getAtoms(name: String): Result[List[WrappedAtom]] =
//      get(atoms, "atoms", name)
//
//    override def getMolecule(name: String): Result[WrappedMolecule] =
//      get(molecule, "molecule", name)
//
//    override def getMolecules(name: String): Result[List[WrappedMolecule]] =
//      get(molecules, "molecules", name)
//
//    private def get[S](m: Map[String, S], `type`: String, name: String): Result[S] =
//       m.get(name)
//        .map(v => Result.successF(v, _.appendAction(
//          s"Got ${`type`} $name",
//          s"Got ${`type`} $name: ${m(name)}"
//        )))
//        .getOrElse(Result.failureF(s"No ${`type`} $name\n${getStatus(name)}"))
//
//    private def getStatus(name: String): String =
//      s"""
//         |Node? ${node.get(name).isDefined}
//         |Nodes? ${nodes.get(name).isDefined}
//         |Atom? ${atom.get(name).isDefined}
//         |Atoms? ${atoms.get(name).isDefined}
//         |Molecule? ${molecule.get(name).isDefined}
//         |Molecules? ${molecules.get(name).isDefined}
//       """.stripMargin
//  }
//
//  case class FakeWrappedNode(
//    atom: Map[String, WrappedAtom] = Map.empty,
//    atoms: Map[String, List[WrappedAtom]] = Map.empty
//  ) extends WrappedNode {
//    override def getAtom(name: String): Result[WrappedAtom] =
//      atom.get(name)
//        .map(v => Result.successF(v, _.appendAction(s"Got atom $name")))
//        .getOrElse(Result.failureF(s"No atom $name"))
//
//    override def getAtoms(name: String): Result[List[WrappedAtom]] =
//      atoms.get(name)
//        .map(v => Result.successF(v, _.appendAction(s"Got atoms $name")))
//        .getOrElse(Result.failureF(s"No atoms $name"))
//
////    def asMolecule: FakeWrappedMolecule =
////      FakeWrappedMolecule(node = Some(this))
//  }
//
//  object FakeWrappedMolecule {
//
//    def nodeAsMolecule(
//      node: FakeWrappedNode
//    ): FakeWrappedMolecule =
//      FakeWrappedMolecule().addAsNodeAndMolecule(node)
//
//    def nodesAsMolecules(
//      nodes: List[FakeWrappedNode]
//    ): FakeWrappedMolecule =
//      FakeWrappedMolecule().addAsNodesAndMolecules(nodes)
//
//    def twoAsNodesAndMolecules(na: FakeWrappedNode, nb: FakeWrappedNode): FakeWrappedMolecule =
//      FakeWrappedMolecule().addTwoAsNodesAndMolecules(na, nb)
//
//    def threeAsNodesAndMolecules(na: FakeWrappedNode, nb: FakeWrappedNode, nc: FakeWrappedNode): FakeWrappedMolecule =
//      FakeWrappedMolecule().addThreeAsNodesAndMolecules(na, nb, nc)
//  }
//
//  case class FakeWrappedMolecule(
//    node: Option[FakeWrappedNode] = None,
//    nodes: Option[List[FakeWrappedNode]]= None,
//    atom: Option[WrappedAtom] = None,
//    atoms: Option[List[WrappedAtom]] = None,
//    molecule: Option[FakeWrappedMolecule] = None,
//    molecules: Option[List[FakeWrappedMolecule]] = None
//  ) extends WrappedMolecule {
//    override def nonNull: Boolean =
//      node.isDefined || nodes.isDefined || atom.isDefined || atoms.isDefined || molecules.isDefined
//
//    def addAsNodeAndMolecule(node: FakeWrappedNode): FakeWrappedMolecule =
//      this.copy(
//        node = Some(node),
//        molecule = Some(FakeWrappedMolecule(node = Some(node)))
//      )
//
//    def addAsNodesAndMolecules(nodes: List[FakeWrappedNode]): FakeWrappedMolecule =
//      this.copy(
//        nodes = Some(nodes),
//        molecule = Some(FakeWrappedMolecule(nodes = Some(nodes))),
////        molecules = Some(List(FakeWrappedMolecule(nodes = Some(nodes))))
//      )
//
//    def addTwoAsNodesAndMolecules(na: FakeWrappedNode, nb: FakeWrappedNode): FakeWrappedMolecule =
//      this.copy(
//        nodes = Some(List(na, nb)),
//        molecule = Some(FakeWrappedMolecule(nodes = Some(List(na, nb)))),
////        molecules = Some(List(FakeWrappedMolecule(nodes = Some(List(na, nb)))))
//        molecules = Some(List(FakeWrappedMolecule(node = Some(na)), FakeWrappedMolecule(node = Some(nb))))
//      )
//
//    def addThreeAsNodesAndMolecules(na: FakeWrappedNode, nb: FakeWrappedNode, nc: FakeWrappedNode): FakeWrappedMolecule =
//      this.copy(
//        nodes = Some(List(na, nb, nc)),
//        molecule = Some(FakeWrappedMolecule(nodes = Some(List(na, nb, nc)))),
////        molecules = Some(List(FakeWrappedMolecule(nodes = Some(List(na, nb, nc)))))
//        molecules = Some(List(
//          FakeWrappedMolecule(node = Some(na)),
//          FakeWrappedMolecule(node = Some(nb)),
//          FakeWrappedMolecule(node = Some(nc))
//        ))
//      )
//
//    override def asNode: Result[WrappedNode] = as(node, "node")
//    override def asNodes: Result[List[WrappedNode]] = as(nodes, "nodes")
//
//    override def asAtom: Result[WrappedAtom] = as(atom, "atom")
//    override def asAtoms: Result[List[WrappedAtom]] = as(atoms, "atoms")
//
//    override def asMolecule: Result[WrappedMolecule] = as(molecule, "molecule")
//    override def asMolecules: Result[List[WrappedMolecule]] = as(molecules, "molecules")
//
//    private def as[S](o: Option[S], name: String): Result[S] =
//      o
//        .map(v => Result.successF(v, _.appendAction(s"Got as $name")))
//        .getOrElse(Result.failureF(s"Not $name\n${getStatus(name)}"))
//
//    private def getStatus(name: String): String =
//      s"""
//         |+++++++++++++++++++++++++++++++++
//         |Node? ${node.isDefined}
//         |Nodes? ${nodes.isDefined}
//         |Atom? ${atom.isDefined}
//         |Atoms? ${atoms.isDefined}
//         |Molecule? ${molecule.isDefined}
//         |Molecules? ${molecules.isDefined}
//         |+++++++++++++++++++++++++++++++++
//       """.stripMargin
//
//  }
//
//  case class FakeWrappedInt(v: Int) extends WrappedAtom {
//    override def value: Value = new IntegerValue(v)
//  }
//
//  case class FakeWrappedString(v: String) extends WrappedAtom {
//    override def value: Value = new StringValue(v)
//  }
//
//  case class FakeWrappedUUID(v: UUID) extends WrappedAtom {
//    override def value: Value = new StringValue(v.toString)
//  }
//
//  case class FakeWrappedLong(v: Long) extends WrappedAtom {
//    override def value: Value = new IntegerValue(v)
//  }
//
//}
