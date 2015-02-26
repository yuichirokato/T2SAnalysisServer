package domain

import scala.slick.driver.MySQLDriver.simple._

case class VerbDic(id: Option[Long], base_form: String, pos: String, cpos: String, read_word: String, meta_words: String)

case class VerbSearchParams(base_form: Option[String], read_word: Option[String], meta_words: Option[String])

object VerbDicTag extends TableQuery(new VerbDicTag(_)) {

  val SEARCH_ALL_PARAM = new VerbSearchParams(None, None, None)

  val findById = this.findBy(_.id)
}

class VerbDicTag(tag: Tag) extends Table[VerbDic](tag, "VerbDic") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def base_form = column[String]("base_form", O.NotNull)
  def pos = column[String]("pos", O.NotNull)
  def cpos = column[String]("cpos", O.NotNull)
  def read_word = column[String]("read_word", O.NotNull)
  def meta_words = column[String]("meta_words", O.Nullable)

  def * = (id.?, base_form, pos, cpos, read_word, meta_words) <> (VerbDic.tupled, VerbDic.unapply)
}
