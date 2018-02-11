package controllers

import domain.PhotoId
import play.api.mvc.PathBindable

object Bindables {

  implicit val bindablePhotoID: PathBindable[PhotoId] =
    PathBindable.bindableString.transform(PhotoId.apply, _.value)

}
