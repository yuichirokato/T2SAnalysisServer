package analysis

import dao.TemplateDicDAO
import domain.TemplateDic

import scala.util.control.Exception._

object Talk {

  def getTemplate(words: List[String]): Either[Failure, Map[String, String]] = {
    val templateDAO = new TemplateDicDAO

    val candidates = words.map { word =>
      templateDAO.searchWithWord(word) match {
        case Right(list) => list
        case Left(e) =>
          println(s"error = ${e.message}")
          List()
      }
    }.flatten.distinct

    println(s"result = ${candidates.toString}")

    getMostHitKeywordTemp(candidates, words) match {
      case Some(str) =>
        println(s"result = $str")
        val action = candidates.filter(_.template == str).head.action
        val category = candidates.filter(_.template == str).head.category
        val feelings = candidates.filter(_.template == str).head.feelings
        Right(Map("template" -> str, "action" -> action.toString, "category" -> category, "feelings" -> feelings.toString))

      case None =>
        Left(Failure("すみません。うまく聞き取れませんでした。もう一度お願いします", FailureType.InternalError))
    }
  }

  def getTemplateWithAD(words: List[String], ad: String): Either[Failure, Map[String, String]] = {
    val templateDAO = new TemplateDicDAO

    val templates = templateDAO.searchWithCategory("音楽") match {
      case Right(list) => list
      case Left(e) =>
        println(s"error = ${e.message}")
        List()
    }

    getMostHitKeywordTemp(templates, words) match {
      case Some(str) =>
        println(s"result = $str")
        val action = templates.filter(_.template == str).head.action
        val category = templates.filter(_.template == str).head.category
        val feelings = templates.filter(_.template == str).head.feelings
        Right(Map("template" -> str, "action" -> action.toString, "category" -> category, "feelings" -> feelings.toString))

      case None =>
        Left(Failure("すみません。うまく聞き取れませんでした。", FailureType.InternalError))
    }
  }

  private def getMostHitKeywordTemp(templates: List[TemplateDic], words: List[String]): Option[String] = {
    val candidates = for {
      word <- words
      tmp <- templates if tmp.keyword.contains(word)
    } yield (word, tmp.template)

    candidates.foreach { case (value1, value2) => println(s"word = $value1, tmp = $value2")}

    println(s"templates = ${candidates.toString}")

    allCatch opt candidates.groupBy(_._2).map { case (key, value) => (value.size, key)}.toList.max._2
  }
}
