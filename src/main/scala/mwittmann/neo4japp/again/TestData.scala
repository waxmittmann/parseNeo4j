package mwittmann.neo4japp.again

import mwittmann.neo4japp.again.N4j.{ListParser, MapParser, NodeParser}
import org.neo4j.driver.internal.types.InternalTypeSystem
import org.neo4j.driver.v1.types.TypeSystem

case class FileData(
  uid: String,
  name: String,
  size: Int
)

case class Artifact(
  uid: String,
  key: String
)

case class WorkflowInstance(
  uid: String,
  inputs: Map[Artifact, Option[FileData]]
)

object Parsers {
  implicit val ts: TypeSystem = InternalTypeSystem.TYPE_SYSTEM

  val fileDataParser: NodeParser[FileData] = { (node: NNode) =>
    for {
      size <- node.intValue("size")
      name <- node.stringValue("name")
      uid <- node.stringValue("uid")
    } yield FileData(uid, name, size)
  }

  val artifactParser: NodeParser[Artifact] = { (node: NNode) =>
    for {
      uid <- node.stringValue("uid")
      key <- node.stringValue("key")
    } yield Artifact(uid, key)
  }

  val artifactParser2 = N4j.asN4jParser(artifactParser)

  val artiAndFdParser: ListParser[List[(Artifact, Option[FileData])]] = ListParser.make { (ele: N4j) =>
    for {
      artiAndFd             <- NList.two(ele)
      arti                  <- artifactParser2(artiAndFd._1)
      maybeFd               <- N4j.optional(fileDataParser)(artiAndFd._2)
    } yield (arti, maybeFd)
  }

  val workflowInstanceParser: MapParser[WorkflowInstance] = { (map: NMap) =>
    for {
      workflowInstanceNode  <- map.getNode("wi")
      workflowInstanceUid   <- workflowInstanceNode.stringValue("uid")
      artisFds              <- map.getList("inputs", artiAndFdParser)
    } yield
      WorkflowInstance(workflowInstanceUid, artisFds.toMap)
  }

}