package v1.news

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import scala.concurrent.Future
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global

case class NewsData(id: Int, title: String, body: String)

object NewsData {
  implicit val NewsDataFormat = Json.format[NewsData]
}

/**
  * A pure non-blocking interface for the NewsRepository.
  */
trait NewsRepository {
  def create(data: NewsData): Future[Unit]

  def list(): Future[Iterable[NewsData]]

  def get(id: Int): Future[Option[NewsData]]
}

/**
  * A trivial implementation for the News Repository.
  */
@Singleton
class NewsRepositoryImpl @Inject()(dbConfigProvider: DatabaseConfigProvider) extends NewsRepository {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  private class NewsTable(tag: Tag) extends Table[NewsData](tag, "news") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def body = column[String]("body")
    def * = (id, title, body) <> ((NewsData.apply _).tupled, NewsData.unapply)
  }

  private val tableQ = TableQuery[NewsTable]

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  private val newsList = List(
    NewsData(1, "title 1", "blog news 1"),
    NewsData(2, "title 2", "blog news 2"),
    NewsData(3, "title 3", "blog news 3"),
    NewsData(4, "title 4", "blog news 4"),
    NewsData(5, "title 5", "blog news 5")
  )

  override def list(): Future[Iterable[NewsData]] = {
    Future.successful {
      logger.trace(s"list: ")
      newsList
    }
  }

  override def get(id: Int): Future[Option[NewsData]] = {
    Future.successful {
      logger.trace(s"get: id = $id")
      newsList.find(news => news.id == id)
    }
  }

  def create(data: NewsData): Future[Unit] = Future {
    (tableQ returning tableQ.map(_.id)) += NewsData(0, "title", "body")
  }

}
