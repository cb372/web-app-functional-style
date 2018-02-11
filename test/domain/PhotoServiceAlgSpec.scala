package domain

import java.io.IOException

import org.scalatest.FlatSpec

import scala.util.{Failure, Success, Try}
import cats.instances.try_._
import domain.ValidationError.NotAPhoto

class PhotoServiceAlgSpec extends FlatSpec {

  behavior of "getting a photo"

  it should "look in the cache before looking in the durable store" in {
    val cache = new CacheAlg[Try] {
      override def getPhoto(id: PhotoId): Try[Option[Photo]] =
        if (id == photoId) Success(Some(photo)) else Success(None)
      override def putPhoto(id: PhotoId, photo: Photo): Try[Unit] = epicFail
    }
    val photoService = new PhotoServiceAlg[Try](
      failingDurableStore,
      cache,
      failingValidation,
      dummyEvents,
      dummyLogging
    )

    assert(photoService.getPhotoById(photoId) == Success(Some(photo)))
  }

  it should "look in the durable store if the cache is a miss" in {
    val durableStore = new DurableStoreAlg[Try] {
      override def getPhoto(id: PhotoId): Try[Option[Photo]] =
        if (id == photoId) Success(Some(photo)) else Success(None)
      override def putPhoto(photo: Photo): Try[PhotoId] = epicFail
    }
    val photoService = new PhotoServiceAlg[Try](
      durableStore,
      emptyCache,
      failingValidation,
      dummyEvents,
      dummyLogging
    )

    assert(photoService.getPhotoById(photoId) == Success(Some(photo)))
  }

  it should "succeed even if both the cache lookup and cache write fail" in {
    val durableStore = new DurableStoreAlg[Try] {
      override def getPhoto(id: PhotoId): Try[Option[Photo]] =
        if (id == photoId) Success(Some(photo)) else Success(None)
      override def putPhoto(photo: Photo): Try[PhotoId] = epicFail
    }
    val photoService = new PhotoServiceAlg[Try](
      durableStore,
      failingCache,
      failingValidation,
      dummyEvents,
      dummyLogging
    )

    assert(photoService.getPhotoById(photoId) == Success(Some(photo)))
  }

  it should "fail if the cache lookup is a miss and the durable store read fails" in {
    val photoService = new PhotoServiceAlg[Try](
      failingDurableStore,
      failingCache,
      failingValidation,
      dummyEvents,
      dummyLogging
    )

    assert(photoService.getPhotoById(photoId).isFailure)
  }

  behavior of "uploading a photo"

  it should "return a validation error if validation fails" in {
    val photoService = new PhotoServiceAlg[Try](
      failingDurableStore,
      failingCache,
      failingValidation,
      dummyEvents,
      dummyLogging
    )

    assert(photoService.uploadPhoto(uploadedBytes) == Success(Left(NotAPhoto)))
  }

  it should "succeed even if the cache write fails" in {
    val photoService = new PhotoServiceAlg[Try](
      emptyDurableStore,
      failingCache,
      successfulValidation,
      dummyEvents,
      dummyLogging
    )

    assert(photoService.uploadPhoto(uploadedBytes) == Success(Right(photoId)))
  }

  it should "succeed even if the event sending fails" in {
    val photoService = new PhotoServiceAlg[Try](
      emptyDurableStore,
      emptyCache,
      successfulValidation,
      failingEvents,
      dummyLogging
    )

    assert(photoService.uploadPhoto(uploadedBytes) == Success(Right(photoId)))
  }

  private val photoId       = PhotoId("abc")
  private val photo         = Photo(Array(1, 2, 3))
  private val uploadedBytes = UploadedBytes(Array(1, 2, 3))

  private val emptyDurableStore = new DurableStoreAlg[Try] {
    override def putPhoto(photo: Photo): Try[PhotoId]      = Success(photoId)
    override def getPhoto(id: PhotoId): Try[Option[Photo]] = Success(None)
  }

  private val failingDurableStore = new DurableStoreAlg[Try] {
    override def putPhoto(photo: Photo): Try[PhotoId]      = epicFail
    override def getPhoto(id: PhotoId): Try[Option[Photo]] = epicFail
  }

  private val emptyCache = new CacheAlg[Try] {
    override def getPhoto(id: PhotoId): Try[Option[Photo]]      = Success(None)
    override def putPhoto(id: PhotoId, photo: Photo): Try[Unit] = Success(())
  }

  private val failingCache = new CacheAlg[Try] {
    override def getPhoto(id: PhotoId): Try[Option[Photo]]      = epicFail
    override def putPhoto(id: PhotoId, photo: Photo): Try[Unit] = epicFail
  }

  private val successfulValidation = new ValidationAlg[Try] {
    override def validatePhoto(bytes: UploadedBytes): Try[Either[ValidationError, Photo]] =
      Success(Right(photo))
  }

  private val failingValidation = new ValidationAlg[Try] {
    override def validatePhoto(bytes: UploadedBytes): Try[Either[ValidationError, Photo]] =
      Success(Left(NotAPhoto))
  }

  private val dummyEvents = new EventsAlg[Try] {
    override def sendPhotoUploadedEvent(id: PhotoId): Try[Unit] = Success(())
  }

  private val failingEvents = new EventsAlg[Try] {
    override def sendPhotoUploadedEvent(id: PhotoId): Try[Unit] = epicFail
  }

  private val dummyLogging = new LoggingAlg[Try] {
    override def warn(msg: String, e: Throwable): Try[Unit] = Success(())
    override def info(msg: String): Try[Unit]               = Success(())
  }

  def epicFail[A]: Try[A] = Failure(new IOException("oops!"))
}
