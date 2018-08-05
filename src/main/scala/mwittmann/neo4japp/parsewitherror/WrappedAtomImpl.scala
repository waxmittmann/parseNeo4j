package mwittmann.neo4japp.parsewitherror

import scala.collection.JavaConverters._

import mwittmann.neo4japp.parsewitherror.N4j.{Result, tryCatch}
import org.neo4j.driver.v1.Value

case class WrappedAtomImpl(value: Value) extends WrappedAtom {
  override def asLong: Result[Long] =
    tryCatch(
      value.asLong(),
      s"$value not a ???"
    )

  override def asString: Result[String] =
    tryCatch(
      value.asString(),
      s"$value not a ???"
    )
}
