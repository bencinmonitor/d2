package models

import play.api.libs.json.{Reads, __}
import play.api.libs.functional.syntax._

case class Location(locationType: String, coordinates: Seq[Double])

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