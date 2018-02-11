package controllers

import play.api.mvc._
import domain._

class PhotoController(controllerComponents: ControllerComponents)
    extends AbstractController(controllerComponents) {

  val list = Action {
    Ok(views.html.list(List(PhotoId(123), PhotoId(456))))
  }

  def getPhoto(id: PhotoId) = TODO

  val uploadPhoto = TODO

}
