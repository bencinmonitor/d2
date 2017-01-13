package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Station(key: String, address: String, location: Location, prices: Map[String, Double])

object Station {

  import reactivemongo.bson._
  import extra.ExtraBSONHandlers._

  implicit val locationWrites = Location.LocationWriter

  implicit val stationReads: Reads[Station] = (
    (__ \ "key").read[String] and
      (__ \ "address").read[String] and
      (__ \ "loc").read[Location] and
      (__ \ "prices").read[Map[String, Double]]
    ) (Station.apply _)

  implicit object StationWriter extends BSONDocumentWriter[Station] {
    def write(station: Station): BSONDocument = {
      BSONDocument(
        "key" -> station.key,
        "address" -> station.address,
        "loc" -> station.location,
        "prices" -> station.prices.map(pair => BSONDocument(
          "type" -> pair._1.replace("-", "_"), "price" -> pair._2)
        )
      )
    }
  }

}

