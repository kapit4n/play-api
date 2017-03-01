package v1.news

import javax.inject.{Inject, Provider}

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json._

/**
  * DTO for displaying news information.
  */
case class NewsResource(id: String, link: String, title: String, body: String)

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
        "body" -> news.body
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
    val data = NewsData(0, newsInput.title, newsInput.body)
    // We don't actually create the news, so return what we have
    newsRepository.create(data).map { res =>
      createNewsResource(res)
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

  def find: Future[Iterable[NewsResource]] = {
    newsRepository.list().map { newsDataList =>
      newsDataList.map(newsData => createNewsResource(newsData))
    }
  }

  private def createNewsResource(p: NewsData): NewsResource = {
    NewsResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }

}
