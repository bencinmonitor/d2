package controllers

import java.net.URLEncoder
import java.util.Calendar
import javax.inject._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.ws._
import play.modules.reactivemongo._
import play.filters.gzip.{Gzip, GzipFilter}
import play.api.libs.streams._
import akka.actor._

import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.streams._
import akka.actor.PoisonPill
import akka.stream._

import scala.util.Success
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.collection.JavaConverters._
import reactivemongo.play.json._
import reactivemongo.bson._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader}
import reactivemongo.play.json.collection._
import models.Location
import models.Station
import models.Station._
import helpers.SlugifyText
import org.sedis.{Pool => RedisPool, _}
import Dress._
import akka.event.LoggingReceive
import redis.clients.jedis._
import play.libs.Akka
import akka.actor._
import akka.pattern.ask
import akka.actor._

import scala.concurrent.ExecutionContext.Implicits.global

class PingActor(hub: ActorRef) extends Actor with ActorLogging {
  //TODO: Replace this with Redis psubscribe
  private var scheduler: Cancellable = _

  import scala.concurrent.duration._
  scheduler = context.system.scheduler.schedule(
    initialDelay = 1 seconds, interval = 3 seconds, receiver = self,
    message = "[tick]"
  )

  override def preStart() = {
    println("PingActor actor preStart()")
  }

  def receive = LoggingReceive {
    case x:String => {
      println(s"PingActor ~> ${x}")
      hub ! s"Event: ${x}"
    }
    case _ => println("This is PingActor, whatever.")
  }

  override def postStop(): Unit = { scheduler.cancel() }
}

class MyWebSocketActor(out: ActorRef, hub: ActorRef) extends Actor with ActorLogging {
  import play.api.libs.json.JsValue
  override def preStart() = hub ! ConnectionMade()

  def receive = LoggingReceive {
    case msg: JsValue =>
      out ! Json.obj("got" -> msg)
    case BroadcastMessage(message) =>
      out ! Json.obj("event"->"time", "message" -> message, "created_at" -> Calendar.getInstance().getTime())
    case _ =>
      out ! Json.toJson(Json.obj("error" -> "Sorry, only JSON!"))
  }
}

class HubActor extends Actor with ActorLogging {
  var connections = Set.empty[ActorRef]

  def receive = {
    case ConnectionMade() =>
      println("HubActor ~> Connection was made.")
      connections += sender
      context watch sender
    case x:String =>
      println(s"HubActor got String~> ${x}")
      self ! Broadcast(x)
    case Broadcast(message) =>
      connections.foreach { actor =>
        println(s"Sending to actor ${actor}")
        actor ! BroadcastMessage(message)
      }
    case _ => println("HubActor,...")
  }
}

case class ConnectionMade()
case class ConnectionBroken()
case class Broadcast(message:String)
case class BroadcastMessage(message: String)

case class Message(nickname: String, userId: Int, msg: String)
object Subscribe







class FeedController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {
  import akka.actor._

  object HubActor {
    lazy val connections = system.actorOf(Props[HubActor])
    def apply() : ActorRef = connections
  }

  object MyWebSocketActor {
    var connections = 0
    def props(out: ActorRef) = {
      connections += 1
      Props(new MyWebSocketActor(out, HubActor()))
    }
  }

  val ping = system.actorOf(Props(classOf[PingActor], HubActor()))

  def index() = Action {
    Ok("feed here.")
  }


  def ws = WebSocket.accept[JsValue, JsValue] { implicit request =>
    ActorFlow.actorRef(MyWebSocketActor.props)
  }

}
