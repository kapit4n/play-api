package v1.news

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes and URLs to the NewsResource controller.
  */
class NewsRouter @Inject()(controller: NewsController) extends SimpleRouter {
  val prefix = "/v1/news"

  def link(id: Int): String = {
    import com.netaporter.uri.dsl._
    val url = prefix / id.toString
    url.toString()
  }

  def linkComment(newsId: Int, id: Int): String = {
    import com.netaporter.uri.dsl._
    val url = prefix / newsId.toString / "comments" / id.toString
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.process

    case POST(p"/$id/comments") =>
      controller.processComments

    case GET(p"/$id/reaction") =>
      controller.processReaction(id)

    case GET(p"/$id") =>
      controller.show(id)

    case GET(p"/$id/comments") =>
      controller.comments(id)

    case DELETE(p"/$id") =>
      controller.delete(id)


  }



}
