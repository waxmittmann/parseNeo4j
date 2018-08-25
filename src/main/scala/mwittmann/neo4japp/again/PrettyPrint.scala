package mwittmann.neo4japp.again

import org.neo4j.driver.v1.Record
import org.neo4j.driver.v1.types.TypeSystem

object PrettyPrint {

  def print(record: Record)(implicit ts: TypeSystem): String = {
    printi(N4j.wrap(record), "")
  }

  private def printi(n4j: N4j, indent: String)(implicit ts: TypeSystem): String = {
    n4j match {
      case NNull => s"$indent:NULL"

      case n: NNode =>
        s"$indent{\n" + n.asMap.mapValues(v => printi(v, indent + "  ")).map { case (k, v) => s"$indent$k: $v" }.mkString("\n") + s"\n$indent}"

      case NList(li) => s"$indent[\n" + li.map(v => printi(v, indent + "  ")).mkString(", ") + s"\n$indent]"

      case m: NMap =>
        s"$indent{\n" + m.asMap.mapValues(v => printi(v, indent + "  ")).map { case (k, v) => s"$indent$k: $v" }.mkString("\n") + s"\n$indent}"

      case NInt(v) => indent + v.toString

      case NFloat(v) => indent + v.toString

      case NString(v) => indent + v.toString

      case NBoolean(v) => indent + v.toString
    }
  }


}
