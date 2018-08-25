//package mwittmann.neo4japp.parsewitherror
//
//import java.util
//import java.util.UUID
//import scala.util.Random
//import scala.collection.JavaConverters._
//
//import cats.syntax._
//import cats.implicits._
//import org.neo4j.driver.v1.Record
//import org.neo4j.driver.v1.summary.ResultSummary
//import org.specs2.mutable.Specification
//
//import mwittmann.neo4japp.db.WrappedDriver
//import mwittmann.neo4japp.parsewitherror.ParseN4j._
//import mwittmann.neo4japp.parsewitherror.WrappedAtomImpl.Implicits._
//
//class ParseN4jSpec extends Specification {
//
//  def randomLabel: String = s"L_${Random.alphanumeric.take(20).mkString}"
//
//  "when doing a simple example" should {
//    "work" in {
//      val rl = randomLabel
//
//      val wd: WrappedDriver = WrappedDriver.local
//      val uid1 = UUID.randomUUID()
//      val uid2 = UUID.randomUUID()
//      wd.unsafeTx {
//        _.run(s"CREATE (v :A :$rl { uid: '${uid1.toString}', attr: 'a' })").summary()
//      }
//      wd.unsafeTx {
//        _.run(s"CREATE (v :A :$rl { uid: '${uid2.toString}', attr: 'b' })").summary()
//      }
//
//      val parse = wd.unsafeTx {
//        _.run(s"MATCH (v :$rl :A) RETURN v")
//      }
//      val resultLi: List[Record] = parse.list().asScala.toList
//
//
//      case class A(uid: UUID, attr: String)
//
//      val nodeParser: NodeParser[A] = { node =>
//        for {
//          uid <- node.getAtomAs[UUID]("uid")
//          attr <- node.getAtomAs[String]("attr")
//        } yield A(uid, attr)
//      }
//
//      val parser = ParseN4j.parseRecords("v", nodeParser)
//      val r: Result[List[A]] = parser(resultLi)
//
//      println(r)
//
//      r should beRight(containTheSameElementsAs(List(
//        A(uid1, "a"),
//        A(uid2, "b")
//      )))
//    }
//
//    "fail" in {
//      val rl = randomLabel
//
//      val wd: WrappedDriver = WrappedDriver.local
//      val uid1 = UUID.randomUUID()
//      wd.unsafeTx {
//        _.run(s"CREATE (v :A :$rl { uid: '${uid1.toString}', attr: 'a' })").summary()
//      }
//
//      val parse = wd.unsafeTx {
//        _.run(s"MATCH (v :$rl :A) RETURN v")
//      }
//      val resultLi: List[Record] = parse.list().asScala.toList
//
//
//      case class A(uid: UUID, attr: Int)
//
//      val nodeParser: NodeParser[A] = { node =>
//        for {
//          uid   <- node.getAtomAs[UUID]("uid")
//          attr  <- node.getAtomAs[Int]("attr")
//        } yield A(uid, attr)
//      }
//
//      val parser = ParseN4j.parseRecords("v", nodeParser)
//      val r: Result[List[A]] = parser(resultLi)
//
//      println(r)
//
//      r should beLeft
//      r.left.get._1 mustEqual "Wasn't an int: \"a\""
//    }
//
//  }
//
//  "when doing a more complex example" should {
//    "work" in {
//      val rl = randomLabel
//
//      val wd: WrappedDriver = WrappedDriver.local
//      val uidA = UUID.randomUUID()
//      val uidB1 = UUID.randomUUID()
//      val uidB2 = UUID.randomUUID()
//      val uidB3 = UUID.randomUUID()
//
//      wd.unsafeStatement(
//        s"""
//           |CREATE
//           |  (a :A :$rl { uid: '${uidA.toString}', attr: 'a' }),
//           |  (a) -[:AB]-> (b1 :B :$rl { uid: '${uidB1.toString}', attr: 'b1' }),
//           |  (a) -[:AB]-> (b2 :B :$rl { uid: '${uidB2.toString}', attr: 'b2' }),
//           |  (a) -[:AB]-> (b3 :B :$rl { uid: '${uidB3.toString}', attr: 'b3' })
//           |""".stripMargin).summary()
//
//      val parse = wd.unsafeTx {
//        _.run(s"MATCH (a :$rl :A) --> (b :$rl :B) RETURN a, collect(b) AS bs")
//      }
//
//      val resultLi: List[Record] = parse.list().asScala.toList
//
//      case class A(uid: UUID, attr: String, bs: List[B])
//      case class B(uid: UUID, attr: String)
//
//      val bParser: NodeParser[B] = { node =>
//        for {
//          uid <- node.getAtomAs[UUID]("uid")
//          attr <- node.getAtomAs[String]("attr")
//        } yield B(uid, attr)
//      }
//
//      val aParser: RecordParser[A] = { (record: WrappedRecord) =>
//        for {
//          node  <- record.getNode("a")
//          uid   <- node.getAtomAs[UUID]("uid")
//          attr  <- node.getAtomAs[String]("attr")
//          rawBs <- record.getNodes("bs")
//          bs    <- rawBs.map(bParser).sequence
//        } yield A(uid, attr, bs)
//      }
//
//      val r: Result[List[A]] = resultLi.map((v: Record) => aParser(new WrappedRecordImpl(v))).sequence
//
//      r must beRight
//      val r2 = r.right.get
//      r2.size mustEqual 1
//      r2.head mustEqual A(uidA, "a", List(B(uidB1, "b1"), B(uidB2, "b2"), B(uidB3, "b3")))
//    }
//  }
//}
