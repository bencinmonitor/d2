import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter
import play.filters.cors.CORSFilter

class Filters @Inject() (gzipFilter: GzipFilter, corsFilter: CORSFilter) extends HttpFilters {
  def filters = Seq(corsFilter, gzipFilter)
}