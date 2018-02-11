package domain

import scala.language.higherKinds

abstract class DurableStoreAlg[F[_]] {

  def getPhoto(id: PhotoId): F[Option[Photo]]

  def putPhoto(photo: Photo): F[PhotoId]

}
