package application

import java.util.UUID

import domain.{DurableStoreAlg, Photo, PhotoId}
import infrastructure.S3Client

class S3DurableStore(s3Client: S3Client) extends DurableStoreAlg[PhotoServiceOp] {

  override val listPhotoIds: PhotoServiceOp[Seq[PhotoId]] = op { requestId =>
    // pretend we are sending the request ID to the backend here...
    s3Client.listKeys("photos/").map { keys =>
      keys.collect {
        case key if key.startsWith("photos/") => PhotoId(key.replaceFirst("photos/", ""))
      }
    }
  }

  override def getPhoto(id: domain.PhotoId): PhotoServiceOp[Option[Photo]] = op { requestId =>
    // pretend we are sending the request ID to the backend here...
    s3Client.get(s"photos/$id").map(_.map(Photo))
  }

  override def putPhoto(photo: Photo): PhotoServiceOp[PhotoId] = op { requestId =>
    // pretend we are sending the request ID to the backend here...
    val photoId = PhotoId(UUID.randomUUID().toString)
    s3Client
      .put(s"photos/$photoId", photo.bytes)
      .map(_ => photoId)
  }

}
