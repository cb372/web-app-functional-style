package controllers

import java.nio.file.Files
import java.util.UUID

import akka.util.ByteString
import application.PhotoServiceOp
import play.api.mvc._
import domain._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import monix.execution.Scheduler.Implicits.global
import play.api.http.HttpEntity

class PhotoController(controllerComponents: ControllerComponents,
                      photoService: PhotoServiceAlg[PhotoServiceOp])
    extends AbstractController(controllerComponents) {

  import PhotoController._

  private val WithRequestId = controllerComponents.actionBuilder.andThen[RequestWithId](
    new ActionTransformer[Request, RequestWithId] {
      override protected def executionContext: ExecutionContext =
        controllerComponents.executionContext
      override protected def transform[A](request: Request[A]): Future[RequestWithId[A]] = {
        val requestId =
          RequestId(request.headers.get("X-Request-ID").getOrElse(UUID.randomUUID().toString))
        Future.successful(RequestWithId[A](request, requestId))
      }
    }
  )

  val list = WithRequestId.async { reqWithId =>
    val task   = photoService.listPhotoIds.run(reqWithId.requestId)
    val future = task.runAsync
    future.transform {
      case Success(photoIds) =>
        Success(Ok(views.html.list(photoIds)))
      case Failure(e) =>
        Logger.warn("Failed to list photos", e)
        Success(InternalServerError("Oops, something went wrong"))
    }
  }

  def getPhoto(id: PhotoId) = WithRequestId.async { reqWithId =>
    val task   = photoService.getPhotoById(id).run(reqWithId.requestId)
    val future = task.runAsync
    future.transform {
      case Success(Some(photo)) =>
        Success(
          Result(ResponseHeader(OK),
                 HttpEntity.Strict(ByteString(photo.bytes), contentType = Some("image/png"))))
      case Success(None) =>
        Success(NotFound("Photo not found"))
      case Failure(e) =>
        Logger.warn("Failed to get photo", e)
        Success(InternalServerError("Oops, something went wrong"))
    }
  }

  val uploadPhoto = WithRequestId.async(controllerComponents.parsers.multipartFormData) {
    reqWithId =>
      reqWithId.request.body.file("file") match {
        case Some(file) =>
          // note: in reality, reading the whole file into memory is not a good idea, especially on the request thread
          val bytes  = UploadedBytes(Files.readAllBytes(file.ref.path))
          val task   = photoService.uploadPhoto(bytes).run(reqWithId.requestId)
          val future = task.runAsync
          future.transform {
            case Success(Right(photoId)) =>
              Success(Created(s"Successfully uploaded photo. ID = $photoId"))
            case Success(Left(validationError)) =>
              Success(BadRequest("Invalid photo file uploaded"))
            case Failure(e) =>
              Logger.warn("Failed to upload photo", e)
              Success(InternalServerError("Oops, something went wrong"))
          }
        case None =>
          Future.successful(BadRequest("File missing"))
      }
  }

}

object PhotoController {

  case class RequestWithId[A](request: Request[A], requestId: RequestId)

}
