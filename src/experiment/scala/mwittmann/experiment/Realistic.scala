//package mwittmann.experiment
//
//import java.util.UUID
//
//import cats.syntax._
//import cats.implicits._
//
//import mwittmann.neo4japp.experiment.Realistic.InstancesBookmark
//import mwittmann.neo4japp.parsewitherror.{ParseN4j, WrappedNode, WrappedRecord}
//import mwittmann.neo4japp.parsewitherror.ParseN4j.{MoleculeParser, NodeParser, RecordParser, Result}
//import mwittmann.neo4japp.parsewitherror.WrappedAtomImpl.Implicits._
//import mwittmann.neo4japp.parsewitherror.ParseN4j._
//
//object Realistic {
//
//  case class FileData(uid: UUID, size: Long, hashcode: String)
//
//  case class Artifact(uid: UUID, name: String, attributes: List[String])
//  case class BindingKey(uid: UUID, key: String)
//  case class BindingValue(uid: UUID, value: String)
//
//  case class Instance(
//    uid: UUID,
//    status: String,
//    inputs: List[(Artifact, FileData)],
//    ouputs: List[Artifact],
//    bindings: Map[BindingKey, BindingValue]
//  )
//
//
//  implicit val fd: NodeParser[FileData] = { fd =>
//    for {
//      uid <- fd.getAtomAs[UUID]("uid")
//      size <- fd.getAtomAs[Long]("size")
//      hashcode <- fd.getAtomAs[String]("hashcode")
//    } yield FileData(uid, size, hashcode)
//  }
//
//  implicit val artifact: NodeParser[Artifact] = { fd: WrappedNode =>
//    for {
//      uid <- fd.getAtomAs[UUID]("uid")
//      name <- fd.getAtomAs[String]("name")
//      hashcode <- fd.getAtomsAs[String]("attributes")
//    } yield Artifact(uid, name, hashcode)
//  }
//
//  implicit val bk: NodeParser[BindingKey] = { fd: WrappedNode =>
//    for {
//      uid <- fd.getAtomAs[UUID]("uid")
//      name <- fd.getAtomAs[String]("key")
//    } yield BindingKey(uid, name)
//  }
//
//  implicit val bv: NodeParser[BindingValue] = { fd: WrappedNode =>
//    for {
//      uid <- fd.getAtomAs[UUID]("uid")
//      name <- fd.getAtomAs[String]("value")
//    } yield BindingValue(uid, name)
//  }
//
//  def i: RecordParser[Instance] = { (i: WrappedRecord) =>
//    val x: Result[Instance] =
//      for {
//        uid <- i.getAtomAs[UUID]
//        status <- i.getAtomAs[String]
//        inputs    <- i.getMoleculesAs[(Artifact, Option[FileData])]
//        outputs   <- i.getNodesAs[Artifact]
//        bindings  <- i.getMoleculesAs[(BindingKey, BindingValue)]
//      } yield {
//        Instance(uid, status, inputs, outputs, bindings)
//      }
//
//    x
//  }
//
//  /*
//
//      RETURN uid, instances
//
//   */
//
//  val is: RecordParser[InstancesBookmark] = { (i: WrappedRecord) =>
//    for {
//      uid       <- i.getAtomAs[UUID]("uid")
//      instances <- i.getMoleculesAs[Instance]("instances")
//    } yield InstancesBookmark(uid, instances)
//  }
//
//
//
//
//  val query =
//    """
//      |MATCH (i :INSTANCE {uid: $workflowUid}),
//      |(i) -[:INPUT]-> (in :ARTIFACT),
//      |(in) -[:DATA]-> (fd :FILE_DATA),
//      |WITH i, collect([in, fd]) AS inputs
//      |(o) -[:OUTPUT]-> (out :ARTIFACT)
//      |WITH i, inputs, collect(out) AS outputs
//      |(i) -[:BINDS]-> (b :BINDING) -[:HAS] -> (v :VALUE)
//      |RETURN i, inputs, outputs, collect([b, v]) AS bindings
//    """.stripMargin
//
//
//
//  def main(args: Array[String]): Unit = {
//    println("Hi")
//  }
//
//
//
//}
