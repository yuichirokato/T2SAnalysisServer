package dao

import java.sql.SQLException

import analysis.{Failure, FailureType}
import config.Configuration
import domain._

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta.MTable

class TemplateDicDAO extends Configuration {

  val db = Database.forURL(url = s"jdbc:mysql://$dbHost:$dbPort/$dbName", user = dbUser, password = dbPassword, driver = "com.mysql.jdbc.Driver")

  val templateDic = TableQuery[TemplateDicTag]

  db.withTransaction { implicit session =>
    if (MTable.getTables("template_dic").list.isEmpty) templateDic.ddl.create
  }

  def create(template: TemplateDic): Either[Failure, TemplateDic] = {
    dbProcWithSession { implicit session =>
      val id = templateDic.insert(template)

      Right(template.copy(id = Some(id)))
    }
  }

  def update(id: Long, template: TemplateDic): Either[Failure, TemplateDic] = {
    dbProcWithSession { implicit session =>
      templateDic.filter(_.id === template.id).update(template.copy(id = Some(id))) match {
        case 0 => Left(notFoundError(id))
        case _ => Right(template.copy(id = Some(id)))
      }
    }
  }

  def delete(id: Long): Either[Failure, TemplateDic] = {
    dbProcWithSession { implicit session =>
      val query = templateDic.filter(_.id === id)
      val list = query.asInstanceOf[List[TemplateDic]]
      list.size match {
        case 0 => Left(notFoundError(id))
        case _ =>
          query.delete
          Right(list.head)
      }
    }
  }

  def get(id: Long): Either[Failure, TemplateDic] = {
    dbProcWithSession { implicit session =>
      TemplateDicTag.findById(id).firstOption match {
        case Some(temp: TemplateDic) => Right(temp)
        case None => Left(notFoundError(id))
      }
    }
  }

  def search(params: TemplateSearchParams): Either[Failure, List[TemplateDic]] = {
    dbProcWithSession { implicit session =>
      val query = for {
        tmp <- templateDic if {
        Seq(
          params.template.map(tmp.template === _),
          params.keyword.map(k => tmp.keyword like s"%$k%"),
          params.category.map(c => tmp.category like s"%$c%"),
          params.action.map(tmp.action === _),
          params.feelings.map(tmp.feelings === _)
        ).flatten match {
          case Nil => tmp.template === tmp.template
          case seq => seq.reduce(_ || _)
        }
      }
      } yield tmp

      Right(query.run(session).toList)
    }
  }

  def searchWithWord(word: String): Either[Failure, List[TemplateDic]] = {
    search(new TemplateSearchParams(None, Some(word), None, None, None))
  }

  def searchWithCategory(category: String): Either[Failure, List[TemplateDic]] = {
    search(new TemplateSearchParams(None, None, Some(category), None, None))
  }

  protected def databaseError(e: SQLException) = {
    Failure(s"${e.getErrorCode}: ${e.getMessage}", FailureType.DatabaseFailure)
  }

  protected def notFoundError(customerId: Long) = {
    Failure(s"Customer with id=$customerId does not exist", FailureType.NotFound)
  }

  private def dbProcWithSession[T](body: Session => Either[Failure, T]): Either[Failure, T] = {
    try {
      db.withSession { implicit session => body(session)}
    } catch {
      case e: SQLException => Left(databaseError(e))
    }
  }

}
