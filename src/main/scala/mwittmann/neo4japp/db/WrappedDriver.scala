package mwittmann.neo4japp.db

import cats.effect.IO
import scala.collection.JavaConverters._
import org.neo4j.driver.v1._

trait Neo4jDriverClient[T] {
  def tx[S](work: T => IO[S]): IO[S]

  def unsafeTx[S](work: T => S): S
}

object WrappedDriver {
  def local: WrappedDriver = new WrappedDriver("bolt://127.0.0.1/", "neo4j", "test")
}

class WrappedDriver(url: String, user: String, password: String) extends Neo4jDriverClient[Transaction] {

  private val token: AuthToken = AuthTokens.basic(user, password)

  val driver: Driver = GraphDatabase.driver(url, token, Config.build.withoutEncryption().toConfig)

  // `bracket` is the IO way of doing the `finally` bit of `try {...} finally {...}`; important because otherwise the
  // session will not be closed, and the hanging session will cause future neo4j queries to hang
  override def tx[S](work: Transaction => IO[S]): IO[S] = IO(driver.session).bracket { session =>
    for {
      _ <- IO.unit
      txResult <- {
        val tx: Transaction = session.beginTransaction()
        work(tx).map { r => (tx, r) }
      }
    } yield {
      txResult._1.success()
      txResult._1.close()
      txResult._2
    }
  } (session => IO(session.close()))

  def unsafeStatement(s: String, p: Map[String, AnyRef] = Map.empty): StatementResult =
    unsafeTx { _.run(s, p.asJava) }

  def unsafeTx[S](work: Transaction => S): S = {
    val session = driver.session()
    val tx: Transaction = session.beginTransaction()

    try {
      val r = work(tx)
      tx.success()
      r
    } catch {
      case e: Exception => {
        tx.failure()
        throw e
      }
    } finally {
      tx.close()
      session.close()
    }
  }

  def close(): Unit = driver.close()
}
