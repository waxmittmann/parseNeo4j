package mwittmann.neo4japp.again

import scala.collection.JavaConverters._

import cats.effect.IO
import mwittmann.neo4japp.db.WrappedDriver
import org.neo4j.driver.internal.InternalNode
import org.neo4j.driver.internal.types.InternalTypeSystem
import org.neo4j.driver.internal.value.{IntegerValue, StringValue}
import org.neo4j.driver.v1.Value
import org.neo4j.driver.v1.types.TypeSystem
import org.neo4j.values.utils.PrettyPrinter

object Neo4jTest {


  /*
      UNWIND uids AS uid
      MATCH (wi :WORKFLOW_INSTANCE {uid: $uid}),
      (wi) -[:INPUT]-> (in :ARTIFACT),
      OPTIONAL MATCH (in) -[:DESCRIBED_BY]-> (fd: FILE_DATA)
      RETURN wi, collect([in, fd]) AS inputs
   */

  val driver = new WrappedDriver("bolt://localhost:7687", "neo4j", "test")

  def main(args: Array[String]): Unit = {

    implicit val ts: TypeSystem = InternalTypeSystem.TYPE_SYSTEM

//    val r = driver.readTx(tx => IO(tx.run("MATCH (n) RETURN n"))).unsafeRunSync()
//    r.list().asScala.foreach(println)

    val i = WorkflowInstance(
      "232-1231-2313-12312",
      Map(
        Artifact("66413-2231-5213-532", "in1") -> Some(FileData("1368-23235-12323", "blah.txt", 223)),
        Artifact("193912-34391-299-21", "in2") -> None
      )
    )

    try {

      driver.unsafeStatement("MATCH (n) DETACH DELETE n")

      val q =
        """
          |CREATE (i :WORKFLOW_INSTANCE { uid: '111111-111111-111111' }),
          |(i) -[:INPUT]->
          | (a1 :ARTIFACT { uid: '111111-111111-111112', key: 'key1' }) -[:DESCRIPTION]->
          | (fd :FILE_DATA { uid: '111111-111111-111113', name: 'name.txt', size: 2332 }),
          |(i) -[:INPUT]->
          | (a2 :ARTIFACT { uid: '111111-111111-111114', key: 'key2' })
        """.stripMargin
      driver.unsafeStatement(q)

      val findQ =
        """
          |MATCH (i :WORKFLOW_INSTANCE { uid: '111111-111111-111111' }),
          |(i) -[:INPUT]-> (a :ARTIFACT)
          |WITH i, a
          |OPTIONAL MATCH (a) -[:DESCRIPTION]-> (fd :FILE_DATA)
          |RETURN i AS wi, collect([a, fd]) AS inputs
        """.stripMargin

      val result = driver.unsafeStatement(findQ).list().asScala
      println(PrettyPrint.print(result.head))

      println(Parsers.workflowInstanceParser(N4j.wrap(result.head)))

    } catch {
      case e: Exception => println(e)
    }

    driver.close()
  }
}
