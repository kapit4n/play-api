package v1.news

import javax.inject.Inject

import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class NewsFormInput(title: String, body: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class NewsController @Inject()(
    action: NewsAction,
    handler: NewsResourceHandler)(implicit ec: ExecutionContext)
    extends Controller {

  private val form: Form[NewsFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "body" -> text
      )(NewsFormInput.apply)(NewsFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = {
    action.async { implicit request =>
      handler.find.map { news =>
        Ok(Json.toJson(news))
      }
    }
  }

  def process: Action[AnyContent] = {
    action.async { implicit request =>
      processJsonNews()
    }
  }

  def show(id: String): Action[AnyContent] = {
    action.async { implicit request =>
      handler.lookup(id).map { news =>
        Ok(Json.toJson(news))
      }
    }
  }

  private def processJsonNews[A]()(
      implicit request: NewsRequest[A]): Future[Result] = {
    def failure(badForm: Form[NewsFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: NewsFormInput) = {
      handler.create(input).map { news =>
        Created(Json.toJson(news)).withHeaders(LOCATION -> news.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
