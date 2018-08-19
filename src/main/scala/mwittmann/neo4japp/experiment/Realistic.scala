//package mwittmann.neo4japp.experiment
//
//import java.util.UUID
//
//import cats.syntax._
//import cats.implicits._
//
//import mwittmann.neo4japp.experiment.FakeN4j._
//import mwittmann.neo4japp.parsewitherror.{ParseN4j, WrappedNode, WrappedRecord}
//import mwittmann.neo4japp.parsewitherror.ParseN4j._
//import mwittmann.neo4japp.parsewitherror.WrappedAtomImpl.Implicits._
//import mwittmann.neo4japp.parsewitherror.ParseN4j.Implicits._
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
//    inputs: List[(Artifact, Option[FileData])],
//    ouputs: List[Artifact],
//    bindings: Map[BindingKey, BindingValue]
//  )
//
//  case class InstancesBookmark(
//    uid: UUID,
//    instances: List[Instance]
//  )
//
////  implicit val fd: NodeParser[FileData] = { fd =>
//  implicit val fd: RecordParser[FileData] = { fd =>
//    for {
//      uid <- fd.getAtomAs[UUID]("uid")
//      size <- fd.getAtomAs[Long]("size")
//      hashcode <- fd.getAtomAs[String]("hashcode")
//    } yield FileData(uid, size, hashcode)
//  }
//
//  implicit val artifact: RecordParser[Artifact] = { fd =>
//    for {
//      uid <- fd.getAtomAs[UUID]("uid")
//      name <- fd.getAtomAs[String]("name")
//      hashcode <- fd.getAtomsAs[String]("attributes")
//    } yield Artifact(uid, name, hashcode)
//  }
//
//  implicit val bk: RecordParser[BindingKey] = { fd =>
//    for {
//      uid <- fd.getAtomAs[UUID]("uid")
//      name <- fd.getAtomAs[String]("key")
//    } yield BindingKey(uid, name)
//  }
//
//  implicit val bv: RecordParser[BindingValue] = { fd =>
//    for {
//      uid <- fd.getAtomAs[UUID]("uid")
//      name <- fd.getAtomAs[String]("value")
//    } yield BindingValue(uid, name)
//  }
//
////  import ParseN4j.moleculeFromNode
//
////  val fdm: MoleculeParser[FileData] = moleculeFromNode(fd)
////  val fdm: RecordParser[FileData] = moleculeFromNode(fd)
//
//
//  def instanceParser: RecordParser[Instance] = { (i: WrappedRecord) =>
//    for {
//      inst      <- i.getNode("i")
//      uid       <- inst.getAtomAs[UUID]("uid")
//      status    <- inst.getAtomAs[String]("status")
//
//      inputs    <- i.getRecordsAs[(Artifact, Option[FileData])]("inputs")
//      outputs   <- i.getNodesAs[Artifact]("outputs")
//      bindings  <- i.getMoleculesAs[(BindingKey, BindingValue)]("bindings")
//    } yield {
//      Instance(uid, status, inputs, outputs, bindings.toMap)
//    }
//  }
//
//  def bookmarkParser: RecordParser[InstancesBookmark] = { (i: WrappedRecord) =>
//    val x: Result[InstancesBookmark] =
//      for {
//        uid       <- i.getAtomAs[UUID]
//        instances <- i.getMoleculesAs[Instance]
//      } yield InstancesBookmark(uid, instances)
//    x
//  }
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
//  val anInstance: FakeWrappedRecord = FakeBuilders.fakeInstance
//  //  println(anInstance)
//  pprint.pprintln(anInstance)
//
//
//  val aBookmark: FakeWrappedRecord = FakeBuilders.fakeBookmark
//
//
//  def main(args: Array[String]): Unit = {
//
////    val rp: RecordParser[List[(Artifact, Option[FileData])]] =
////      (r: WrappedRecord) => r.getMoleculesAs[(Artifact, Option[FileData])]("inputs")
////    val r2 = rp(FakeWrappedRecord(molecules = Map("inputs" -> List(FakeBuilders.fakeInputs)))).run(ParseState.empty)
////    println(r2)
////    println("<><><><><><><><><>")
////    println(r2.left.map(_.state.actions.map(_._2).reverse.mkString("\n--\n")))
//
//    //    val result = instanceParser(anInstance)
//    //    val r = result.run(ParseState.empty)
//    //    println(r)
//    //    println("   \n\n\n")
//    //    println(r.left.map(_.state.actions.map(_._2).reverse.mkString("\n")))
//
//    val result = instanceParser(aBookmark)
//    val r = result.run(ParseState.empty)
//    println(r)
//    println("   \n\n\n")
//    println(r.left.map(_.state.actions.map(_._2).reverse.mkString("\n")))
//  }
//
//}
