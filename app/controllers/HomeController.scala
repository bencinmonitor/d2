package controllers

import javax.inject.{Singleton}
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.ExecutionContext

@Singleton
class HomeController extends Controller {

  def index() = Action {
    Ok(Json.obj("status" -> "ok"))
  }
}
