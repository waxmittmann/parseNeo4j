package mwittmann.neo4japp

import mwittmann.neo4japp.db.WrappedGraphDatabase

// Note: Won't work unless dropping down to scala 2.11.8, which is incompatible with the dependencies as they currently are
// defined
object GraphDbMain {

  val graphDb = WrappedGraphDatabase.graphDb

  def main(args: Array[String]): Unit = {
    try {
      graphDb.execute("CREATE (n { attr: 'a'})")
      val r = graphDb.execute("MATCH (n) RETURN n")
      while (r.hasNext) {
        println(r.next())
      }
    } finally {
      graphDb.shutdown()
    }
  }

}
