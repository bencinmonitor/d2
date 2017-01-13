package models

import org.scalatestplus.play._

class LocationSpec extends PlaySpec {

  "Distance" must {
    "between two different points" in {
      val a = Location("A", Seq(15.10632852, 46.35917056))
      val b = Location("B", Seq(14.50348527, 46.05795559))
      a.distanceTo(b) === 74.4
    }

    "at the same point" in {
      val a = Location("A", Seq(15.0, 15.0))
      a.distanceTo(a) === 0.0
    }
  }
}
