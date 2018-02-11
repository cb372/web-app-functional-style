package domain

import scala.language.higherKinds

abstract class CacheAlg[F[_]] {

  def getPhoto(id: PhotoId): F[Option[Photo]]

  def putPhoto(id: PhotoId, photo: Photo): F[Unit]

}
