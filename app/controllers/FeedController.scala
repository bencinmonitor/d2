package controllers

import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.streams._
import akka.stream._
import actors.{WebSocketActor, PingActor}
import akka.actor._

class FeedController @Inject() (implicit system: ActorSystem, materializer: Materializer, @Named("hubActor") hub: ActorRef) extends Controller {
  import akka.actor._

  val ping = system.actorOf(Props(classOf[PingActor], hub))

  def feed = WebSocket.accept[JsValue, JsValue] { implicit request =>
    ActorFlow.actorRef(out => Props(classOf[WebSocketActor], out, hub))
  }
}
