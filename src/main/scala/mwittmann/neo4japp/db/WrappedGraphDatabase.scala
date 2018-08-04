package mwittmann.neo4japp.db

import java.io.File

import org.neo4j.graphdb.{GraphDatabaseService, Transaction}
import org.neo4j.graphdb.factory.GraphDatabaseFactory

object WrappedGraphDatabase {
  val graphDb: GraphDatabaseService = new GraphDatabaseFactory()
    .newEmbeddedDatabaseBuilder( new File("data") )
    //.loadPropertiesFromFile( pathToConfig + "neo4j.conf" )
    .newGraphDatabase()

  val wrappedNeo4jClient  = new WrappedGraphDatabase(graphDb)

}

class WrappedGraphDatabase(db: GraphDatabaseService) {

  def tx[S](work: Transaction => S): S = {
    val tx: org.neo4j.graphdb.Transaction = db.beginTx()

    try {
      val v = work(tx)
      tx.success()
      v
    } catch {
      case e: Throwable => {
        tx.failure()
        throw e
      }
    }
  }

  // Todo: Use readonly transaction
  def readTx[S](work: Transaction => S): S = tx(work)

  def close(): Unit = db.shutdown()
}
