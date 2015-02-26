package dao

import java.sql.SQLException

import analysis.{Failure, FailureType}
import config.Configuration
import domain.{NounSearchParams, NounDic, NounDicTag}

import scala.slick.driver.MySQLDriver.simple._

class NounDicDAO extends Configuration {

  val db = Database.forURL(url = s"jdbc:mysql://$dbHost:$dbPort/$dbName", user = dbUser, password = dbPassword, driver = "com.mysql.jdbc.Driver")

  val nounDic = TableQuery[NounDicTag]

  def create(noun: NounDic): Either[Failure, NounDic] = {
    try {
      val id = db.withSession { implicit session =>
        nounDic.insert(noun)
      }
      Right(noun.copy(id = Some(id)))
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

  def update(id: Long, noun: NounDic): Either[Failure, NounDic] = {
    try {
      db.withSession { implicit session =>
        nounDic.filter(_.id === noun.id).update(noun.copy(id = Some(id))) match {
          case 0 => Left(notFoundError(id))
          case _ => Right(noun.copy(id = Some(id)))
        }
      }
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

  def delete(id: Long): Either[Failure, NounDic] = {
    try {
      db.withTransaction { implicit session =>
        val query = nounDic.filter(_.id === id)
        val list = query.asInstanceOf[List[NounDic]]
        list.size match {
          case 0 => Left(notFoundError(id))
          case _ =>
            query.delete
            Right(list.head)
        }
      }
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

  def get(id: Long): Either[Failure, NounDic] = {
    try {
      db.withSession { implicit sesiion =>
        NounDicTag.findById(id).firstOption match {
          case Some(nounDic: NounDic) => Right(nounDic)
          case None => Left(notFoundError(id))
        }
      }
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

  def search(params: NounSearchParams): Either[Failure, List[NounDic]] = {
    try {
      db.withSession { implicit session =>
        val query = for {
          noun <- nounDic if {
          Seq(
            params.base_form.map(noun.base_form === _),
            params.read_word.map(noun.read_word === _),
            params.meta_words.map(m => noun.meta_words like s"%$m%")
          ).flatten match {
            case Nil => noun.base_form === noun.base_form
            case seq => seq.reduce(_ || _)
          }
        }
        } yield noun

        Right(query.run(session).toList)
      }
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

  protected def databaseError(e: SQLException) = {
    Failure(s"${e.getErrorCode}: ${e.getMessage}", FailureType.DatabaseFailure)
  }

  protected def notFoundError(nounId: Long) = {
    Failure(s"tableName with id=$nounId does not exist", FailureType.NotFound)
  }

}
