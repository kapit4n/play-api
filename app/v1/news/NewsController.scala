package v1.news

import javax.inject.Inject

import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._


import scala.concurrent.{ExecutionContext, Future}

case class NewsFormInput(title: String, body: String, imgUrl: String, likes: Int)

case class CommentsFormInput(newsId: Int, body: String, likes: Int)

/**
  * Takes HTTP requests and produces JSON.
  */
class NewsController @Inject()(
    action: NewsAction, commentsAction: CommentsAction,
    handler: NewsResourceHandler)(implicit ec: ExecutionContext)
    extends Controller{

  private val form: Form[NewsFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "body" -> text,
        "imgUrl" -> text,
        "likes" -> number
      )(NewsFormInput.apply)(NewsFormInput.unapply)
    )
  }

  private val formComments: Form[CommentsFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "newsId" -> number,
        "body" -> text,
        "likes" -> number
      )(CommentsFormInput.apply)(CommentsFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = {
    action.async { implicit request =>
      handler.find.map { news =>
        Ok(Json.toJson(news))
      }
    }
  }

  def comments(id: String): Action[AnyContent] = {
    commentsAction.async { implicit request =>
      handler.findComments(id).map { news =>
        Ok(Json.toJson(news))
      }
    }
  }

  def process: Action[AnyContent] = {
    action.async { implicit request =>
      processJsonNews()
    }
  }

  def processComments: Action[AnyContent] = {
    commentsAction.async { implicit request =>
      processJsonComments()
    }
  }

  def processReaction(id: String): Action[AnyContent] = {
    action.async { implicit request =>
      handler.reaction(id).map { news =>
        Ok(Json.toJson("This is the example"))
      }
    }
  }

  def show(id: String): Action[AnyContent] = {
    action.async { implicit request =>
      handler.lookup(id).map { news =>
        Ok(Json.toJson(news))
      }
    }
  }

  def delete(id: String): Action[AnyContent] = {
    action.async { implicit request =>
      handler.delete(id).map { delNews =>
        Ok(Json.toJson(delNews))
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

  private def processJsonComments[A]()(
      implicit request: CommentsRequest[A]): Future[Result] = {
    def failureComments(badForm: Form[CommentsFormInput]) = {
      Future.successful(BadRequest("badForm.errorsAsJson"))
    }

    def successComments(input: CommentsFormInput) = {
      handler.createComment(input).map { comments =>
        Created(Json.toJson(comments)).withHeaders(LOCATION -> comments.link)
      }
    }

    formComments.bindFromRequest().fold(failureComments, successComments)
  }

}
