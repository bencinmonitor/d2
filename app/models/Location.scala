package models

import math._
import play.api.libs.json.{Reads, __}
import play.api.libs.functional.syntax._

case class Location(locationType: String, coordinates: Seq[Double]) {

  def distanceTo(locationB: Location): Double = helpers.Haversine.haversine(
    coordinates(0), coordinates(1), locationB.coordinates(0), locationB.coordinates(1)
  )
}

object Location {

  import reactivemongo.bson._

  implicit val locationReads: Reads[Location] = (
    (__ \ "type").read[String] and (__ \ "coordinates").read[Seq[Double]]
    ) (Location.apply _)

  implicit object LocationWriter extends BSONDocumentWriter[Location] {
    def write(location: Location): BSONDocument = BSONDocument(
      "type" -> location.locationType,
      "coordinates" -> location.coordinates
    )
  }

}