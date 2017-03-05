package v1.news

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import scala.concurrent.Future
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global

case class NewsData(id: Int, title: String, body: String, likes: Int)

object NewsData {
  implicit val NewsDataFormat = Json.format[NewsData]
}

/**
  * A pure non-blocking interface for the NewsRepository.
  */
trait NewsRepository {
  def create(data: NewsData): Future[NewsData]

  def list(): Future[Iterable[NewsData]]

  def get(id: Int): Future[Option[NewsData]]

  def reaction(id: Int): Future[Option[NewsData]]

  def delete(id: Int): Future[Int]
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
    def likes = column[Int]("likes")
    def * = (id, title, body, likes) <> ((NewsData.apply _).tupled, NewsData.unapply)
  }

  private val tableQ = TableQuery[NewsTable]

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  private val newsList = List(
    NewsData(1, "title 1", "blog news 1", 0),
    NewsData(2, "title 2", "blog news 2", 0),
    NewsData(3, "title 3", "blog news 3", 0),
    NewsData(4, "title 4", "blog news 4", 0),
    NewsData(5, "title 5", "blog news 5", 0)
  )

  override def list(): Future[Iterable[NewsData]] = {
      db.run {
        tableQ.sortBy(m => (m.id)).result
      }
  }

  override def get(id: Int): Future[Option[NewsData]] = db.run {
    tableQ.filter(_.id === id).result.headOption
  }

  def create(data: NewsData): Future[NewsData] = db.run {
    val pair = ("Title", "body", 1)
    (tableQ.map(p => (p.title, p.body, p.likes))
      returning tableQ.map(_.id)
      into ((nameAge, id) => NewsData(id, nameAge._1, nameAge._2, nameAge._3))) += pair
  }

  def reaction(id: Int): Future[Option[NewsData]] = db.run {
    val res = db.run(tableQ.filter(_.id === id).result.headOption)
    val f: Future[Option[NewsData]] = db.run(tableQ.filter(_.id === id).result.headOption)
    f.onSuccess { case s => {
        val q = for { c <- tableQ if c.id === id } yield c.likes
        val updateAction = q.update(s.get.likes + 1)
        db.run(updateAction)
      }
    }
    return res
  }

  def delete(id: Int): Future[Int] = db.run {
    (tableQ.filter(_.id === id)).delete
  }



}
