package domain

import scala.language.higherKinds

abstract class ValidationAlg[F[_]] {

  def validatePhoto(bytes: UploadedBytes): F[Either[ValidationError, Photo]]

}

sealed trait ValidationError

object ValidationError {
  case object NotAPhoto extends ValidationError
}
