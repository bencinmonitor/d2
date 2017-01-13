package helpers

import java.text.Normalizer

object SlugifyText {
  def slugify(str: String): String = {
    Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\w ]", "").replace(" ", "-").toLowerCase
  }
}
