package domain

import scala.slick.driver.MySQLDriver.simple._

case class TemplateDic(id: Option[Long], template: String, keyword: String, category: String, action: Int, errata: String)

case class TemplateSearchParams(template: Option[String], keyword: Option[String], category: Option[String], action: Option[Int], errata: Option[String])

object TemplateDicTag extends TableQuery(new TemplateDicTag(_)) {
  val SEARCH_ALL_PARAM = new TemplateSearchParams(None, None, None, None, None)

  val findById = this.findBy(_.id)
}

class TemplateDicTag(tag: Tag) extends Table[TemplateDic](tag, "template_dic") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def template = column[String]("template", O.NotNull)
  def keyword = column[String]("keyword", O.NotNull)
  def category = column[String]("category", O.NotNull)
  def action = column[Int]("action", O.NotNull)
  def errata = column[String]("errata", O.NotNull)

  def * = (id.?, template, keyword, category, action, errata) <> (TemplateDic.tupled, TemplateDic.unapply)

}
