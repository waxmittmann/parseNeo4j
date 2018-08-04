package mwittmann.neo4japp.db

import cats.effect.IO
import org.neo4j.driver.v1._

trait Neo4jClient[T] {
  def tx[S](work: T => IO[S]): IO[S]

  def readTx[S](work: T => IO[S]): IO[S]
}

class WrappedDriver(url: String, user: String, password: String) extends Neo4jClient[Transaction] {

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

  // Todo: Use readonly transaction
  override def readTx[S](work: Transaction => IO[S]): IO[S] = tx(work)

  def close(): Unit = driver.close()
}
