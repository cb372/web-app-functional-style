package domain

import scala.language.higherKinds

abstract class EventsAlg[F[_]] {

  def sendPhotoUploadedEvent(id: PhotoId): F[Unit]

}
