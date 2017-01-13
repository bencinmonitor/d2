package controllers

import javax.inject._

import models.Location
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
// import play.api.libs.functional.syntax._
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import play.modules.reactivemongo._
import reactivemongo.play.json.collection._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import reactivemongo.play.json._
import reactivemongo.bson._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader}
import reactivemongo.api.collections.bson.BSONCollection

import models.Station
import models.Station._

@Singleton
class StationController @Inject()(val reactiveMongoApi: ReactiveMongoApi, val ws: WSClient)(implicit exec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents {
  lazy val stations = database.map(_.collection("stations"))

  def geocode(rawAddress: Option[String] = None, timeout: FiniteDuration = 2.seconds): Future[Seq[Double]] = {
    if (rawAddress.isEmpty) return Future(Seq.empty[Double])

    val wsRequest = ws.url(s"http://maps.googleapis.com/maps/api/geocode/json")
      .withHeaders(ACCEPT -> "applicaton/json")
      .withQueryString("sensor" -> "false")
      .withQueryString("address" -> rawAddress.get)
      .withRequestTimeout(timeout)

    wsRequest.get().map { wsResponse =>
      (wsResponse.json \ "status").as[String] match {
        case "OK" =>
          val location = (wsResponse.json \ "results") (0) \ "geometry" \ "location"
          Seq[Double]((location \ "lng").as[Double], (location \ "lat").as[Double])
        case _ => Seq.empty[Double]
      }
    }
  }

  def geocode_it(address: String) = Action.async {
    for {location <- geocode(Some(address))} yield Ok(Json.toJson(BSONDocument("coordinates" -> location)))
  }

  def listStations(at: Seq[Double] = Seq.empty[Double], limit: Int, maxDistance: Int): Future[List[Station]] = {
    stations.flatMap {
      var query = BSONDocument("company" -> "petrol")

      if (at.nonEmpty) {
        query = query.add(BSONDocument("loc" -> BSONDocument("$near" -> BSONDocument(
          "$geometry" -> BSONDocument("$type" -> "Point", "coordinates" -> at),
          "$maxDistance" -> maxDistance)
        )))
      }

      var projection = BSONDocument("key" -> 1, "address" -> 1, "loc" -> 1, "updated_at" -> 1, "scraped_url" -> 1)

      projection = projection.add("prices" -> "1")

      _.find(query, projection).cursor[Station]().collect[List](limit)
    }
  }

  def index(near: Option[String] = None, at: Option[String], limit: Int, maxDistance: Int) = Action.async {
    val now = System.nanoTime
    val atLocation = at.fold(Seq.empty[Double])(_.split(",").map(_.toDouble))

    val composition: Future[List[Station]] = if (near.isEmpty && atLocation.nonEmpty) {
      listStations(atLocation, limit, maxDistance)
    } else if (near.nonEmpty) {
      geocode(near).flatMap(listStations(_, limit, maxDistance))
    } else {
      listStations(limit = limit, maxDistance = maxDistance)
    }

    composition.map { stations =>
      Ok(Json.toJson(BSONDocument(
        "stations" -> stations,
        "status" -> "ok",
        "executed_in" -> ((System.nanoTime - now).asInstanceOf[Double] / 1000000000)
      )))
    }
  }
}