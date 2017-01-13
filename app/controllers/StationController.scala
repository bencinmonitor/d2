package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.ws._
import play.modules.reactivemongo._

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
import redis.clients.jedis._

@Singleton
class StationController @Inject()(val reactiveMongoApi: ReactiveMongoApi, val ws: WSClient, val redis: RedisPool)(implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents {

  lazy val stations: Future[JSONCollection] = database.map(_.collection("stations"))

  def geocode(rawAddress: Option[String] = None, timeout: FiniteDuration = 2.seconds, cache: Boolean = false, expireTTL: FiniteDuration = 1.day): Future[Seq[Double]] = {
    if (rawAddress.isEmpty) return Future(Seq.empty[Double])

    /* Lets check if there is address already in REDIS */
    val cacheKey: String = "address:%s".format(SlugifyText.slugify(rawAddress.get))
    val inCacheJson = Json.parse(redis.withJedisClient[String](c => Dress.up(c).get(cacheKey).getOrElse("[]"))).as[Seq[Double]]

    /* Extend expire and return if coordinates are in cache */
    if (inCacheJson.nonEmpty) {
      val extendTTL = redis.withJedisClient(c => Dress.up(c).expire(cacheKey, expireTTL.toSeconds.toInt))
      Logger.info("Extended TTL for key '%s'.".format(cacheKey))
      return Future(inCacheJson)
    }

    /* Actual WS request */
    val wsRequest = ws.url(s"http://maps.googleapis.com/maps/api/geocode/json")
      .withHeaders(ACCEPT -> "applicaton/json")
      .withQueryString("sensor" -> "false")
      .withQueryString("address" -> rawAddress.get)
      .withRequestTimeout(timeout)

    val f = wsRequest.get().map { wsResponse =>
      (wsResponse.json \ "status").as[String] match {
        case "OK" =>
          val location = (wsResponse.json \ "results") (0) \ "geometry" \ "location"
          Seq[Double]((location \ "lng").as[Double], (location \ "lat").as[Double])
        case _ => Seq.empty[Double]
      }
    }

    f.onComplete {
      case Success(coordinates) => {
        if (coordinates.nonEmpty) {
          val setAddress = redis.withJedisClient(c => Dress.up(c).setex(cacheKey, expireTTL.toSeconds.toInt, Json.toJson(coordinates).toString()))
          Logger.info(s"Coordinates for redis ~> ${coordinates} ${setAddress}")
        }
      }
      case _ => ???
    }

    f
  }

  def geocode_it(address: String): Action[AnyContent] = Action.async {
    for {location <- geocode(Some(address))} yield Ok(Json.toJson(BSONDocument("coordinates" -> location)))
  }

  def listStations(at: Seq[Double] = Seq.empty[Double], limit: Int, maxDistance: Int): Future[List[Station]] = {
    val origin: Location = Location("Origin", at)
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

      _.find(query, projection).cursor[Station]().collect[List](limit).map { listOfStations: List[Station] =>
        listOfStations.map { station =>
          if (at.nonEmpty) station.distanceToOrigin = station.location.distanceTo(origin)
          station
        }
      }
    }
  }

  def index(near: Option[String] = None, at: Option[String], limit: Int, maxDistance: Int): Action[AnyContent] = Action.async {
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

  def cache_state() = Action {
    Ok(Json.obj("keys" -> redis.withJedisClient[java.util.Set[String]](
      client => Dress.up(client).keys("address:*")
    ).asScala))
  }
}