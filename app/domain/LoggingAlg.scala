package domain

import scala.language.higherKinds

abstract class LoggingAlg[F[_]] {

  def info(msg: String): F[Unit]

  def warn(msg: String, e: Throwable): F[Unit]

}
