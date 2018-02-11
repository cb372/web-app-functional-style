package controllers

import domain.PhotoId
import play.api.mvc.PathBindable

object Bindables {

  implicit val bindablePhotoID: PathBindable[PhotoId] =
    PathBindable.bindableInt.transform(PhotoId.apply, _.value)

}
