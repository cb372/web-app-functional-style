package domain

import scala.language.higherKinds

abstract class EventsAlg[F[_]] {

  def sendUploadedPhotoEvent(id: PhotoId): F[Unit]

}
