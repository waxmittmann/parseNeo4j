package mwittmann.neo4japp.parsewitherror

import java.util
import java.util.UUID
import scala.util.Random
import scala.collection.JavaConverters._

import mwittmann.neo4japp.db.WrappedDriver
import mwittmann.neo4japp.parsewitherror.ParseN4j.{NodeGetDirect, NodeParser, RecordParser, Result}
import mwittmann.neo4japp.parsewitherror.WrappedAtomImpl.Implicits._
import org.neo4j.driver.v1.Record
import org.neo4j.driver.v1.summary.ResultSummary
import org.specs2.mutable.Specification


class ParseN4jSpec extends Specification {

  def randomLabel: String = s"L_${Random.alphanumeric.take(20).mkString}"

  "when doing a simple example" should {
    "work" in {
      val rl = randomLabel

      val wd: WrappedDriver = WrappedDriver.local
      val uid1 = UUID.randomUUID()
      val uid2 = UUID.randomUUID()
      wd.unsafeTx { _.run(s"CREATE (v :A :$rl { uid: '${uid1.toString}', attr: 'a' })").summary() }
      wd.unsafeTx { _.run(s"CREATE (v :A :$rl { uid: '${uid2.toString}', attr: 'b' })").summary() }

      val parse = wd.unsafeTx { _.run(s"MATCH (v :$rl :A) RETURN v") }
      val resultLi: List[Record] = parse.list().asScala.toList


      case class A(uid: UUID, attr: String)

      val nodeParser: NodeParser[A] = { node =>
        for {
          uid <- node.get[UUID]("uid")
          attr <- node.get[String]("attr")
        } yield A(uid, attr)
      }

      val parser = ParseN4j.parseRecords("v", nodeParser)
      val r: Result[List[A]] = parser(resultLi)

      println(r)

      r should beRight(containTheSameElementsAs(List(
        A(uid1, "a"),
        A(uid2, "b")
      )))
    }

    "fail" in {
      val rl = randomLabel

      val wd: WrappedDriver = WrappedDriver.local
      val uid1 = UUID.randomUUID()
      wd.unsafeTx { _.run(s"CREATE (v :A :$rl { uid: '${uid1.toString}', attr: 'a' })").summary() }

      val parse = wd.unsafeTx { _.run(s"MATCH (v :$rl :A) RETURN v") }
      val resultLi: List[Record] = parse.list().asScala.toList


      case class A(uid: UUID, attr: Int)

      val nodeParser: NodeParser[A] = { node =>
        for {
          uid <- node.get[UUID]("uid")
          attr <- node.get[Int]("attr")
        } yield A(uid, attr)
      }

      val parser = ParseN4j.parseRecords("v", nodeParser)
      val r: Result[List[A]] = parser(resultLi)

      println(r)

      r should beLeft
      r.left.get._1 mustEqual "Wasn't an int: \"a\""
    }

  }

}
