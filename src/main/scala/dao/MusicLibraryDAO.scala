package dao

import java.sql.SQLException

import analysis.{Failure, FailureType}
import config.Configuration
import domain.{MusicLibrary, MusicLibrarySearchParams, MusicLibraryTag}

import scala.slick.driver.MySQLDriver.simple._

class MusicLibraryDAO extends Configuration {

  val db = Database.forURL(url = s"jdbc:mysql://$dbHost:$dbPort/$dbName", user = dbUser, password = dbPassword, driver = "com.mysql.jdbc.Driver")

  val musicLibrary = TableQuery[MusicLibraryTag]

  def create(music: MusicLibrary): Either[Failure, MusicLibrary] = {
    dbProcWithSession { implicit session =>
      val id = musicLibrary.insert(music)
      Right(music.copy(id = Some(id)))
    }
  }

  def update(id: Long, music: MusicLibrary): Either[Failure, MusicLibrary] = {
    dbProcWithSession { implicit session =>
      musicLibrary.filter(_.id === music.id).update(music.copy(id = Some(id))) match {
        case 0 => Left(notFoundError(id))
        case _ => Right(music.copy(id = Some(id)))
      }
    }
  }

  def delete(id: Long): Either[Failure, MusicLibrary] = {
    dbProcWithSession { implicit session =>
      val query = musicLibrary.filter(_.id === id)
      val list = query.asInstanceOf[List[MusicLibrary]]
      list.size match {
        case 0 => Left(notFoundError(id))
        case _ =>
          query.delete
          Right(list.head)
      }
    }
  }

  def get(id: Long): Either[Failure, MusicLibrary] = {
    dbProcWithSession { implicit session =>
      MusicLibraryTag.findById(id).firstOption match {
        case Some(music: MusicLibrary) => Right(music)
        case None => Left(notFoundError(id))
      }
    }
  }

  def search(params: MusicLibrarySearchParams): Either[Failure, List[MusicLibrary]] = {
    dbProcWithSession { implicit session =>
      val query = for {
        music <- musicLibrary if {
        Seq(
          params.title.map(music.title === _),
          params.artists.map(music.artists === _),
          params.favorite.map(music.favorite === _),
          params.category.map(music.category === _)
        ).flatten match {
          case Nil => music.title === music.title
          case seq => seq.reduce(_ || _)
        }
      }
      } yield music

      Right(query.run(session).toList)
    }
  }

  protected def databaseError(e: SQLException) = {
    Failure(s"${e.getErrorCode}: ${e.getMessage}", FailureType.DatabaseFailure)
  }

  protected def notFoundError(tableId: Long) = {
    Failure(s"tableName with id=$tableId does not exist", FailureType.NotFound)
  }

  private def dbProcWithSession[T](body: Session => Either[Failure, T]): Either[Failure, T] = {
    try {
      db.withSession { implicit session => body(session)}
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }
}
