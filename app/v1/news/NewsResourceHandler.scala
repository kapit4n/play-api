package v1.news

import javax.inject.{Inject, Provider}

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json._

/**
  * DTO for displaying news information.
  */
case class NewsResource(id: String, link: String, title: String, body: String, likes: Int)

/**
  * DTO for displaying news information.
  */
case class CommentsResource(id: String, link: String, newsId: Int, body: String, likes: Int)

object NewsResource {

  /**
    * Mapping to write a NewsResource out as a JSON value.
    */
  implicit val implicitWrites = new Writes[NewsResource] {
    def writes(news: NewsResource): JsValue = {
      Json.obj(
        "id" -> news.id,
        "link" -> news.link,
        "title" -> news.title,
        "body" -> news.body,
        "likes" -> news.likes
      )
    }
  }
}

object CommentsResource {

  /**
    * Mapping to write a CommentsResource out as a JSON value.
    */
  implicit val implicitWrites = new Writes[CommentsResource] {
    def writes(comments: CommentsResource): JsValue = {
      Json.obj(
        "id" -> comments.id,
        "link" -> comments.link,
        "newsId" -> comments.newsId,
        "body" -> comments.body,
        "likes" -> comments.likes
      )
    }
  }
}

/**
  * Controls access to the backend data, returning [[NewsResource]]
  */
class NewsResourceHandler @Inject()(
    routerProvider: Provider[NewsRouter],
    newsRepository: NewsRepository)(implicit ec: ExecutionContext) {

  def create(newsInput: NewsFormInput): Future[NewsResource] = {
    val data = NewsData(0, newsInput.title, newsInput.body, 0)
    // We don't actually create the news, so return what we have
    newsRepository.create(data).map { res =>
      createNewsResource(res)
    }
  }

  def createComment(newsInput: CommentsFormInput): Future[CommentsResource] = {
    val data = CommentsData(0, newsInput.newsId, newsInput.body, 0)
    // We don't actually create the news, so return what we have
    newsRepository.createComment(data).map { res =>
      createCommentsResource(res)
    }
  }

  def lookup(id: String): Future[Option[NewsResource]] = {
    val newsFuture = newsRepository.get(id.toInt)
    newsFuture.map { maybeNewsData =>
      maybeNewsData.map { newsData =>
        createNewsResource(newsData)
      }
    }
  }

  def reaction(id: String): Future[Option[NewsResource]] = {
    val newsFuture = newsRepository.reaction(id.toInt)
    newsFuture.map { maybeNewsData =>
      maybeNewsData.map { newsData =>
        createNewsResource(newsData)
      }
    }
  }

  def find: Future[Iterable[NewsResource]] = {
    newsRepository.list().map { newsDataList =>
      newsDataList.map(newsData => createNewsResource(newsData))
    }
  }

  def findComments(id: String): Future[Iterable[CommentsResource]] = {
    newsRepository.listComments(id.toInt).map { commentsDataList =>
      commentsDataList.map(commentsData => createCommentsResource(commentsData))
    }
  }

  def delete(id: String): Future[Int] = {
    newsRepository.delete(id.toInt).map { res => res
    }
  }

  private def createNewsResource(p: NewsData): NewsResource = {
    NewsResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body, p.likes)
  }

  private def createCommentsResource(p: CommentsData): CommentsResource = {
    CommentsResource(p.id.toString, routerProvider.get.linkComment(p.newsId, p.id), p.newsId, p.body, p.likes)
  }

}
