package mwittmann.neo4japp.parsewitherror

import java.util.UUID
import scala.collection.JavaConverters._
import cats.implicits._
import cats.syntax._

import org.neo4j.driver.v1.Value

import mwittmann.neo4japp.parsewitherror.ParseN4j.{AtomParser, tryCatch}

object WrappedAtomImpl {
  object Implicits {
    implicit val parseLong: AtomParser[Long] = (wa: WrappedAtom) => {
      tryCatch(wa.value.asLong(), s"Wasn't a long: ${wa.value}",
        _.appendAction(s"Parsed long ${wa.value.asLong()}"))
    }

    implicit val parseInt: AtomParser[Int] = (wa: WrappedAtom) => {
      tryCatch(wa.value.asInt(), s"Wasn't an int: ${wa.value}",
        _.appendAction(s"Parsed int ${wa.value.asInt()}"))
    }

    implicit val parseString: AtomParser[String] = (wa: WrappedAtom) => {
      tryCatch(wa.value.asString(), s"Wasn't a string: ${wa.value}",
        _.appendAction(s"Parsed string ${wa.value.asString()}"))
    }

    implicit val parseBoolean: AtomParser[Boolean] = (wa: WrappedAtom) => {
      tryCatch(wa.value.asBoolean(), s"Wasn't a boolean: ${wa.value}",
        _.appendAction(s"Parsed boolean ${wa.value.asBoolean()}"))
    }

    implicit val parseUuid: AtomParser[UUID] = (wa: WrappedAtom) =>
      parseString(wa).flatMap { s =>
        tryCatch(UUID.fromString(s), s"Wasn't a uuid: ${wa.value}",
          _.appendAction(s"Parsed UID ${UUID.fromString(s)}"))
      }

//      parseString(wa).right.flatMap { s =>
//        tryCatch(UUID.fromString(s), s"Wasn't a uuid: ${wa.value}")
//      }

//    implicit def parseList[S](implicit sParser: AtomParser[S]): AtomParser[List[S]] = (wa: WrappedAtom) => {
//      tryCatch {
//        wa.
//        val ss = wa.value.asList(v => v).asScala.toList
//        ss.map(sPar)
//      } {
//
//      }
//    }

  }
}

case class WrappedAtomImpl(value: Value) extends WrappedAtom
