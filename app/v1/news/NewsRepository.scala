package v1.news

import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

final case class NewsData(id: NewsId, title: String, body: String)

class NewsId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object NewsId {
  def apply(raw: String): NewsId = {
    require(raw != null)
    new NewsId(Integer.parseInt(raw))
  }
}

/**
  * A pure non-blocking interface for the NewsRepository.
  */
trait NewsRepository {
  def create(data: NewsData): Future[NewsId]

  def list(): Future[Iterable[NewsData]]

  def get(id: NewsId): Future[Option[NewsData]]
}

/**
  * A trivial implementation for the News Repository.
  */
@Singleton
class NewsRepositoryImpl @Inject() extends NewsRepository {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  private val newsList = List(
    NewsData(NewsId("1"), "title 1", "blog news 1"),
    NewsData(NewsId("2"), "title 2", "blog news 2"),
    NewsData(NewsId("3"), "title 3", "blog news 3"),
    NewsData(NewsId("4"), "title 4", "blog news 4"),
    NewsData(NewsId("5"), "title 5", "blog news 5")
  )

  override def list(): Future[Iterable[NewsData]] = {
    Future.successful {
      logger.trace(s"list: ")
      newsList
    }
  }

  override def get(id: NewsId): Future[Option[NewsData]] = {
    Future.successful {
      logger.trace(s"get: id = $id")
      newsList.find(news => news.id == id)
    }
  }

  def create(data: NewsData): Future[NewsId] = {
    Future.successful {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
