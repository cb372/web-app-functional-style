package object domain {

  case class PhotoId(value: String) extends AnyVal {
    override def toString: String = value.toString
  }

  case class UploadedBytes(bytes: Array[Byte]) extends AnyVal

  case class Photo(bytes: Array[Byte]) extends AnyVal

  case class RequestId(value: String) extends AnyVal

}
