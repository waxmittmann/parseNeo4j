package mwittmann.neo4japp

import cats.effect.IO
import scala.collection.JavaConverters._
import mwittmann.neo4japp.db.WrappedDriver

object DriverMain {

  val driver = new WrappedDriver("bolt://localhost:7687", "neo4j", "test")

  def main(args: Array[String]): Unit = {
    val r = driver.readTx(tx => IO(tx.run("MATCH (n) RETURN n"))).unsafeRunSync()
    r.list().asScala.foreach(println)
  }

}
