package application

import domain.{Photo, UploadedBytes, ValidationAlg, ValidationError}

object LenientValidation extends ValidationAlg[PhotoServiceOp] {

  override def validatePhoto(
      uploadedBytes: UploadedBytes): PhotoServiceOp[Either[ValidationError, Photo]] =
    // here we would validate that the bytes are a valid PNG, the photo is not too big or small, etc.
    pure(Right(Photo(uploadedBytes.bytes)))

}
