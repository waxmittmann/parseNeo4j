package mwittmann.neo4japp.again

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

import cats._
import cats.syntax._
import cats.implicits._
import cats.instances._
import org.neo4j.driver.v1.types.{Node, TypeSystem}
import org.neo4j.driver.v1.{Record, Value}
import mwittmann.neo4japp.again.N4j.{ListParser, MapParser, NodeParser}
import org.neo4j.driver.internal.InternalNode
import org.neo4j.driver.internal.types.InternalTypeSystem
import org.neo4j.driver.internal.value.{IntegerValue, ListValue, StringValue}

object N4j {
  type EE[S] = Either[String, S]

  type NodeParser[S] = NNode => Either[String, S]
  type MapParser[S] = NMap => Either[String, S]
  type ListParser[S] = NList => Either[String, S]
  type N4jParser[S] = N4j => Either[String, S]

  object ListParser {
    def make[S](rawParser: N4j => Either[String, S]): ListParser[List[S]] = { li: NList =>
      li.li.map(rawParser).sequence[EE, S]
    }
  }

  def asN4jParser[S <: N4j, T](parser: S => Either[String, T])(implicit ct: ClassTag[S]): N4jParser[T] = {
    case correct: S => parser(correct)
    case bad        => Left(s"Wrong type $bad")
  }

  def optional[T](parser: NodeParser[T]): N4jParser[Option[T]] = {
    case NNull    => Right(None)
    case n: NNode => parser(n).map(Some.apply)
    case bad      => Left(s"Wrong type $bad")
  }

//  def wrap(record: Record)(implicit ts: TypeSystem): N4j = {
//    NMapImpl(record.asMap[N4j](wrap).asScala.toMap)
//  }

  def wrap(record: Record)(implicit ts: TypeSystem): NMap = {
    NMapImpl(record.asMap[N4j](wrap).asScala.toMap)
  }

  def wrap(values: List[Value])(implicit ts: TypeSystem): NList = {
    NList(values.map(wrap))
  }

  def wrap(value: Value)(implicit ts: TypeSystem): N4j = {
    if (value.`type`() == ts.LIST())          wrap(value.asList(v => v).asScala.toList)
    else if (value.`type`() == ts.NODE())     NNodeImpl(value.asNode())
    else if (value.`type`() == ts.BOOLEAN())  NBoolean(value.asBoolean())
    else if (value.`type`() == ts.FLOAT())    NFloat(value.asFloat())
    else if (value.`type`() == ts.INTEGER())  NInt(value.asInt())
    else if (value.`type`() == ts.STRING())   NString(value.asString())
    else if (value.`type`() == ts.NULL())     NNull
    else                                      throw new Exception(s"Unsupported type ${value.`type`()}")
  }

  def wrap(record: Node)(implicit ts: TypeSystem): NNode = ???

}

sealed trait N4j

case object NNull extends N4j

object NNode {
  def get(n4j: N4j): Either[String, NNode] = n4j match {
    case n: NNode => Right(n)
    case bad      => Left(s"Wrong type $bad")
  }

  def get[S](n4j: N4j, np: NodeParser[S]): Either[String, S] =
    get(n4j) flatMap np

}

sealed trait NNode extends N4j {
  def intValue(key: String)(implicit ts: TypeSystem): Either[String, Int]

  def stringValue(key: String)(implicit ts: TypeSystem): Either[String, String]

  def asMap(implicit ts: TypeSystem): Map[String, N4j]
}

case class NNodeImpl(node: Node) extends NNode {
  def intValue(key: String)(implicit ts: TypeSystem): Either[String, Int] = N4j.wrap(node.get(key)) match {
    case NInt(v)  => Right(v)
    case bad      => Left(s"Unexpected type $bad when getting $key")
  }

  def stringValue(key: String)(implicit ts: TypeSystem): Either[String, String] = N4j.wrap(node.get(key)) match {
    case NString(v)  => Right(v)
    case bad         => Left(s"Unexpected type $bad when getting $key")
  }

  def asMap(implicit ts: TypeSystem): Map[String, N4j] =
    node.asMap((v: Value) => v).asScala.mapValues((v: Value) => N4j.wrap(v)).toMap
}

object NList {
  def get(n4j: N4j): Either[String, NList] = n4j match {
    case n: NList => Right(n)
    case bad      => Left(s"Wrong type $bad")
  }

  def two(n4j: N4j): Either[String, (N4j, N4j)] = n4j match {
    case NList(a :: b :: Nil) => Right((a, b))
    case bad      => Left(s"Wrong type $bad")
  }

}

case class NList(li: List[N4j]) extends N4j

sealed trait NMap extends N4j {
  def getList(str: String): Either[String, NList]

  def getList[S](str: String, lp: ListParser[S]): Either[String, S] =
    getList(str) flatMap lp

  def getNode(str: String): Either[String, NNode]

  def get(str: String): N4j

  def asMap(implicit ts: TypeSystem): Map[String, N4j]
}

case class NMapImpl(map: Map[String, N4j]) extends NMap {
  def getList(str: String): Either[String, NList] = map.getOrElse(str, NNull) match {
    case n: NList => Right(n)
    case bad      => Left(s"Wrong type $bad")
  }

  def getNode(str: String): Either[String, NNode] = map.getOrElse(str, NNull) match {
    case n: NNode => Right(n)
    case bad      => Left(s"Wrong type $bad when getting $str")
  }

  def get(str: String): N4j = map.getOrElse(str, NNull)

  def asMap(implicit ts: TypeSystem): Map[String, N4j] = map
}

object NValue {
  def getInt(n4j: N4j): Either[String, NInt] = n4j match {
    case n: NInt  => Right(n)
    case bad      => Left(s"Wrong type $bad")
  }

  def getString(n4j: N4j): Either[String, NString] = n4j match {
    case n: NString => Right(n)
    case bad        => Left(s"Wrong type $bad")
  }

}

sealed trait NValue extends N4j
case class NInt(v: Int) extends NValue
case class NFloat(v: Float) extends NValue
case class NString(v: String) extends NValue
case class NBoolean(v: Boolean) extends NValue
