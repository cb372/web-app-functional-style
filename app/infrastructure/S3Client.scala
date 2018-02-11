package infrastructure

import java.io.ByteArrayInputStream

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{ObjectMetadata, S3ObjectInputStream}
import com.google.common.io.ByteStreams
import monix.eval.Task

import scala.util.{Failure, Success, Try}

class S3Client(bucket: String, underlying: AmazonS3) {

  def get(key: String): Task[Option[Array[Byte]]] = Task {
    Try(underlying.getObject(bucket, key)) match {
      case Success(s3object) => toByteArray(s3object.getObjectContent)
      case Failure(_) =>
        None // can't be bothered trying to separate "object not found" exceptions from real errors
    }
  }

  def put(key: String, value: Array[Byte]): Task[Unit] = Task {
    underlying.putObject(bucket, key, new ByteArrayInputStream(value), new ObjectMetadata())
  }

  private def toByteArray(stream: S3ObjectInputStream): Option[Array[Byte]] = {
    if (stream == null)
      None
    else {
      try {
        Some(ByteStreams.toByteArray(stream))
      } finally {
        stream.close()
      }
    }
  }
}
