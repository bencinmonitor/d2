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
import actors.{HubActor, WebSocketActor, PingActor}
import akka.event.LoggingReceive
import redis.clients.jedis._
import play.libs.Akka
import akka.actor._
import akka.pattern.ask
import akka.actor._

import actors._

import scala.concurrent.ExecutionContext.Implicits.global

class FeedController @Inject() (implicit system: ActorSystem, materializer: Materializer, @Named("hubActor") hub: ActorRef) extends Controller {
  import akka.actor._

  val ping = system.actorOf(Props(classOf[PingActor], hub))

  def feed = WebSocket.accept[JsValue, JsValue] { implicit request =>
    ActorFlow.actorRef(out => Props(classOf[WebSocketActor], out, hub))
  }
}
