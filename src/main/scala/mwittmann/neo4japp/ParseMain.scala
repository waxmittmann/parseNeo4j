//package mwittmann.neo4japp
//
//import java.util.UUID
//
//import cats.syntax._
//import cats.implicits._
//
//import mwittmann.neo4japp.parsewitherror.ParseN4j.{MoleculeParser, NodeParser, Result, optional, three}
//import mwittmann.neo4japp.parsewitherror.{WrappedNode, WrappedRecord}
//import mwittmann.neo4japp.parsewitherror.WrappedAtomImpl.Implicits._
//import mwittmann.neo4japp.parsewitherror.ParseN4j._
//import mwittmann.neo4japp.parsewitherror.ParseN4j.Implicits._
//
//object ParseMain {
//
//  def main(args: List[String]): Unit = {
//
//    val result: WrappedRecord = ???
//
//    val workflowInstanceNode: Result[WrappedNode] = result.getNode("workflowInstance")
//
//    val inputParts = for {
//      inputMolecules <- result.getMolecules("inputs")
//
//      inputParts <- {
//        inputMolecules.map { molecule =>
//          for {
//            inputParts <- molecule.asMolecules
//            artifactDefnNode <- inputParts(0).asNode
//            artifactNode <- inputParts(1).asNode
//            fileDataNode <- inputParts(2).asNode
//          } yield (artifactDefnNode, artifactNode, fileDataNode)
//        }.sequence[Result, (WrappedNode, WrappedNode, WrappedNode)]
//      }
//    } yield inputParts
//  }
//
//  case class Artifact(uid: UUID, blobUid: UUID)
//
//  trait ArtifactDefn
//  trait FileData
//  val artifactDefnParser: NodeParser[Artifact] = { node =>
//    for {
//      uidAtom <- node.getAtom("uid")
//      uid     <-  uidAtom.as[UUID]
//
//      blobUidAtom <- node.getAtom("blobUid")
//      blobUid <-  blobUidAtom.as[UUID]
//    } yield Artifact(uid, blobUid)
//  }
//
//  val artifactParser: NodeParser[ArtifactDefn] = ???
//
//  val fileData: NodeParser[FileData] = ???
//
//  val inputsParser: MoleculeParser[(Artifact, ArtifactDefn, Option[FileData])] =
////    three(artifactDefnParser, artifactParser, optional(fileData))
//    // Todo: Why doesn't implicit conversion work here now?
//    three(
//      moleculeFromNodeI(artifactDefnParser),
//      moleculeFromNodeI(artifactParser),
//      optional(moleculeFromNodeI(fileData))
//    )
//
//}
