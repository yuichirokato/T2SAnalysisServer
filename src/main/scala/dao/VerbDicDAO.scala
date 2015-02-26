package dao

import java.sql.SQLException

import analysis.{AnalyzedToken, Failure, FailureType}
import config.Configuration
import domain.{NounDic, VerbDic, VerbDicTag, VerbSearchParams}

import scala.slick.driver.MySQLDriver.simple._

object VerbDicDAO {

  val dao = new VerbDicDAO

  def checkOverlap(tokens: List[AnalyzedToken]): Boolean = {
    dao.search(VerbDicTag.SEARCH_ALL_PARAM) match {
      case Right(list) =>
        val baseForms = list.map(_.base_form)
        tokens.map(t => !baseForms.contains(t.baseForm)).reduce(_ && _)
      case Left(e) => false
    }
  }

}

class VerbDicDAO extends Configuration {

  val db = Database.forURL(url = s"jdbc:mysql://$dbHost:$dbPort/$dbName", user = dbUser, password = dbPassword, driver = "com.mysql.jdbc.Driver")
  val verbDic = TableQuery[VerbDicTag]

  def create(verb: VerbDic): Either[Failure, VerbDic] = {
    dbProcWithSession { implicit session =>
      val id = verbDic.insert(verb)

      Right(verb.copy(id = Some(id)))
    }
  }

  def update(id: Long, verb: VerbDic): Either[Failure, VerbDic] = {
    dbProcWithSession { implicit session =>
      verbDic.filter(_.id === verb.id).update(verb.copy(id = Some(id))) match {
        case 0 => Left(notFoundError(id))
        case _ => Right(verb.copy(id = Some(id)))
      }
    }
  }

  def delete(id: Long): Either[Failure, VerbDic] = {
    dbProcWithSession { implicit session =>
      val query = verbDic.filter(_.id === id)
      val list = query.asInstanceOf[List[VerbDic]]
      list.size match {
        case 0 => Left(notFoundError(id))
        case _ =>
          query.delete
          Right(list.head)
      }
    }
  }

  def get(id: Long): Either[Failure, VerbDic] = {
    dbProcWithSession { implicit session =>
      VerbDicTag.findById(id).firstOption match {
        case Some(verb: VerbDic) => Right(verb)
        case None => Left(notFoundError(id))
      }
    }
  }

  def search(params: VerbSearchParams): Either[Failure, List[VerbDic]] = {
    dbProcWithSession { implicit session =>
      val query = for {
        verb <- verbDic if {
        Seq(
          params.base_form.map(verb.base_form === _),
          params.read_word.map(verb.read_word === _),
          params.meta_words.map(m => verb.meta_words like s".*$m.*")
        ).flatten match {
          case Nil => verb.base_form === verb.base_form
          case seq => seq.reduce(_ && _)
        }
      }
      } yield verb

      Right(query.run(session).toList)
    }
  }

  protected def databaseError(e: SQLException) = Failure(s"${e.getErrorCode}: ${e.getMessage}", FailureType.DatabaseFailure)

  protected def notFoundError(verbId: Long) = Failure(s"tableName with id=$verbId does not exist", FailureType.NotFound)

  private def dbProcWithSession[T](body: Session => Either[Failure, T]): Either[Failure, T] = {
    try {
      db.withSession { implicit session => body(session) }
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

}
