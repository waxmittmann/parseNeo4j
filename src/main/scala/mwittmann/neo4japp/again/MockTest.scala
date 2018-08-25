package mwittmann.neo4japp.again

import org.neo4j.driver.v1.types.TypeSystem
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

object MockTest {


  /*
      UNWIND uids AS uid
      MATCH (wi :WORKFLOW_INSTANCE {uid: $uid}),
      (wi) -[:INPUT]-> (in :ARTIFACT),
      OPTIONAL MATCH (in) -[:DESCRIBED_BY]-> (fd: FILE_DATA)
      RETURN wi, collect([in, fd]) AS inputs
   */


  def main(args: Array[String]): Unit = {

    implicit val ts: TypeSystem = InternalTypeSystem.TYPE_SYSTEM

    val base: NMap =
      NMapImpl(Map(
        "wi"      -> NNodeImpl(new InternalNode(0l, List.empty.asJava, Map(
          "uid" -> (new StringValue("abc") : Value)
        ).asJava)),
        "inputs"  -> NList(List(
          NList(List(
            NNodeImpl(new InternalNode(0l,
              List.empty.asJava,
              Map(
                "uid" -> (new StringValue("abc") : Value),
                "key" -> (new StringValue("abc") : Value)
              ).asJava
            )),
            NNodeImpl(new InternalNode(0l,
              List.empty.asJava,
              Map(
                "uid" -> (new StringValue("abc") : Value),
                "size" -> (new IntegerValue(23) : Value),
                "name" -> (new StringValue("snerkalr") : Value)
              ).asJava
            ))
          )),

          NList(List(
            NNodeImpl(new InternalNode(0l,
              List.empty.asJava,
              Map(
                "uid" -> (new StringValue("abc2") : Value),
                "key" -> (new StringValue("abc2") : Value)
              ).asJava
            )),
            NNodeImpl(new InternalNode(0l,
              List.empty.asJava,
              Map(
                "uid" -> (new StringValue("abc2") : Value),
                "size" -> (new IntegerValue(24) : Value),
                "name" -> (new StringValue("snerkalr2") : Value)
              ).asJava
            ))
          )),

          NList(List(
            NNodeImpl(new InternalNode(0l,
              List.empty.asJava,
              Map(
                "uid" -> (new StringValue("abc3") : Value),
                "key" -> (new StringValue("abc3") : Value)
              ).asJava
            )),
            NNull
          ))
        ))
      ))

    println(Parsers.workflowInstanceParser(base))
  }
}
