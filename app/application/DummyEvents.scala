package application

import domain.{EventsAlg, LoggingAlg, PhotoId}

class DummyEvents(logging: LoggingAlg[PhotoServiceOp]) extends EventsAlg[PhotoServiceOp] {

  override def sendPhotoUploadedEvent(id: PhotoId): PhotoServiceOp[Unit] =
    logging.info(s"Pretending to send a 'photo uploaded' event for photo $id")

}
