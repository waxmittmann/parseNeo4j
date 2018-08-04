package mwittmann.neo4japp.utils

object CatchError {
  case class WrappedClientException(ce: Exception) extends Exception(s"WrappedClientException: ${ce.getMessage}")

  def catchError[S](statement: => S): S = try {
    statement
  } catch {
    case e: Exception => throw WrappedClientException(e)
  }
}
