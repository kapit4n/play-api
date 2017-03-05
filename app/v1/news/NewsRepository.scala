package v1.news

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import scala.concurrent.Future
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global

case class NewsData(id: Int, title: String, body: String, likes: Int)

case class CommentsData(id: Int, newsId: Int, body: String, likes: Int)

object NewsData {
  implicit val NewsDataFormat = Json.format[NewsData]
}

object CommentsData {
  implicit val CommentsDataFormat = Json.format[CommentsData]
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

  private val newsQ = TableQuery[NewsTable]

  private class CommentsTable(tag: Tag) extends Table[CommentsData](tag, "comments") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def newsId = column[Int]("newsId")
    def body = column[String]("body")
    def likes = column[Int]("likes")
    def news = foreignKey("SUP_FK", newsId, newsQ)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def * = (id, newsId, body, likes) <> ((CommentsData.apply _).tupled, CommentsData.unapply)
  }

  private val commentsQ = TableQuery[CommentsTable]

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  override def list(): Future[Iterable[NewsData]] = {
      db.run {
        newsQ.sortBy(m => (m.id)).result
      }
  }

  override def get(id: Int): Future[Option[NewsData]] = db.run {
    newsQ.filter(_.id === id).result.headOption
  }

  def create(data: NewsData): Future[NewsData] = db.run {
    val pair = ("Title", "body", 1)
    (newsQ.map(p => (p.title, p.body, p.likes))
      returning newsQ.map(_.id)
      into ((nameAge, id) => NewsData(id, nameAge._1, nameAge._2, nameAge._3))) += pair
  }

  def reaction(id: Int): Future[Option[NewsData]] = db.run {
    val res = db.run(newsQ.filter(_.id === id).result.headOption)
    val f: Future[Option[NewsData]] = db.run(newsQ.filter(_.id === id).result.headOption)
    f.onSuccess { case s => {
        val q = for { c <- newsQ if c.id === id } yield c.likes
        val updateAction = q.update(s.get.likes + 1)
        db.run(updateAction)
      }
    }
    return res
  }

  def delete(id: Int): Future[Int] = db.run {
    (newsQ.filter(_.id === id)).delete
  }
}
