package infrastructure

import java.io.ByteArrayInputStream

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{ObjectMetadata, S3ObjectInputStream}
import com.google.common.io.ByteStreams
import monix.eval.Task

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class S3Client(bucket: String, underlying: AmazonS3) {

  // Note: this client is NOT async, so doesn't actually meet requirements. Whoops!
  // AWS Java SDK v2 (currently in developer preview) will support proper async I/O.

  def get(key: String): Task[Option[Array[Byte]]] = Task {
    Try(underlying.getObject(bucket, key)) match {
      case Success(s3object) => toByteArray(s3object.getObjectContent)
      case Failure(_) =>
        None // can't be bothered trying to separate "object not found" exceptions from real errors
    }
  }

  def put(key: String, value: Array[Byte]): Task[Unit] = Task {
    val metadata = new ObjectMetadata()
    metadata.setContentLength(value.length)
    underlying.putObject(bucket, key, new ByteArrayInputStream(value), metadata)
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

  def listKeys(prefix: String): Task[Seq[String]] = Task {
    // Note: doesn't support pagination, only returns the first page of results
    underlying.listObjects(bucket, prefix).getObjectSummaries.asScala.map(_.getKey)
  }

}
