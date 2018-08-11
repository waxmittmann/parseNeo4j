package mwittmann.neo4japp.experiment

import java.util.UUID

import mwittmann.neo4japp.parsewitherror.{ParseN4j, WrappedNode, WrappedRecord}
import mwittmann.neo4japp.parsewitherror.ParseN4j.{MoleculeParser, NodeParser, RecordParser, Result}

import mwittmann.neo4japp.parsewitherror.WrappedAtomImpl.Implicits._
import mwittmann.neo4japp.parsewitherror.ParseN4j.Implicits._

object Realistic {

  case class FileData(uid: UUID, size: Long, hashcode: String)

  case class Artifact(uid: UUID, name: String, attributes: List[String])
  case class BindingKey(uid: UUID, key: String)
  case class BindingValue(uid: UUID, value: String)

  case class Instance(
    uid: UUID,
    status: String,
    inputs: List[(Artifact, Option[FileData])],
    ouputs: List[Artifact],
    bindings: Map[BindingKey, BindingValue]
  )


  implicit val fd: NodeParser[FileData] = { fd =>
    for {
      uid <- fd.getAtomAs[UUID]("uid")
      size <- fd.getAtomAs[Long]("size")
      hashcode <- fd.getAtomAs[String]("hashcode")
    } yield FileData(uid, size, hashcode)
  }

  implicit val artifact: NodeParser[Artifact] = { fd: WrappedNode =>
    for {
      uid <- fd.getAtomAs[UUID]("uid")
      name <- fd.getAtomAs[String]("name")
      hashcode <- fd.getAtomsAs[String]("attributes")
    } yield Artifact(uid, name, hashcode)
  }

  implicit val bk: NodeParser[BindingKey] = { fd: WrappedNode =>
    for {
      uid <- fd.getAtomAs[UUID]("uid")
      name <- fd.getAtomAs[String]("key")
    } yield BindingKey(uid, name)
  }

  implicit val bv: NodeParser[BindingValue] = { fd: WrappedNode =>
    for {
      uid <- fd.getAtomAs[UUID]("uid")
      name <- fd.getAtomAs[String]("value")
    } yield BindingValue(uid, name)
  }

  import ParseN4j.moleculeFromNode

  val fdm: MoleculeParser[FileData] = moleculeFromNode(fd)

  implicitly[MoleculeParser[FileData]]
  implicitly[MoleculeParser[Option[FileData]]]
  implicitly[MoleculeParser[(Artifact, Option[FileData])]]

  def i: RecordParser[Instance] = { (i: WrappedRecord) =>
    val x: Result[Instance] =
      for {
        uid <- i.getAtomAs[UUID]("i.uid")
        status <- i.getAtomAs[String]("i.status")
        inputs    <- i.getMoleculesAs[(Artifact, Option[FileData])]("inputs")
        outputs   <- i.getNodesAs[Artifact]("outputs")
        bindings  <- i.getMoleculesAs[(BindingKey, BindingValue)]("bindings")
      } yield {
        Instance(uid, status, inputs, outputs, bindings.toMap)
      }

    x
  }


  val query =
    """
      |MATCH (i :INSTANCE {uid: $workflowUid}),
      |(i) -[:INPUT]-> (in :ARTIFACT),
      |(in) -[:DATA]-> (fd :FILE_DATA),
      |WITH i, collect([in, fd]) AS inputs
      |(o) -[:OUTPUT]-> (out :ARTIFACT)
      |WITH i, inputs, collect(out) AS outputs
      |(i) -[:BINDS]-> (b :BINDING) -[:HAS] -> (v :VALUE)
      |RETURN i, inputs, outputs, collect([b, v]) AS bindings
    """.stripMargin



  def main(args: Array[String]): Unit = {
    println("Hi")
  }



}
