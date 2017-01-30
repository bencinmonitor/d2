package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Station(key: String, name: String, address: String, location: Location, prices: Map[String, Double]) {
  var distanceToOrigin: Double = 0.0
}

object Station {

  import reactivemongo.bson._
  import extra.ExtraBSONHandlers._

  implicit val locationWrites = Location.LocationWriter

  implicit val stationReads: Reads[Station] = (
    (__ \ "key").read[String] and
      (__ \ "name").read[String] and
      (__ \ "address").read[String] and
      (__ \ "loc").read[Location] and
      (__ \ "prices").read[Map[String, Double]]
    ) (Station.apply _)

  implicit object StationWriter extends BSONDocumentWriter[Station] {
    def write(station: Station): BSONDocument = {
      var stationJson = BSONDocument(
        "key" -> station.key,
        "name" -> station.name,
        "address" -> station.address,
        "loc" -> station.location,
        "prices" -> station.prices.map(pair => BSONDocument(
          "type" -> pair._1.replace("-", "_"), "price" -> pair._2)
        )
      )

      if (station.distanceToOrigin != 0.0) stationJson = stationJson.add("distance" -> station.distanceToOrigin)

      stationJson
    }
  }

}

