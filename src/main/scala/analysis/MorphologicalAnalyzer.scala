package analysis

import cabocha.Cabocha
import enums.PosDiv
import org.atilika.kuromoji.{Token, Tokenizer}
import implicits.Conversions._
import utils.CabochaUtils

import scala.collection.JavaConversions._

case class AnalyzedToken(baseForm: String, pos: String, cpos: String, read: String, meta: String = "none")

object MorphologicalAnalyzer {

  val POS_NOUN = "名詞"
  val POS_PRONOUN = "代名詞"
  val POS_VERB = "動詞"
  val CABOCHA_PATH = "/usr/local/bin/cabocha"

  def analize(word: String): List[String] = {
    println(s"word = $word")
    val cabocha = Cabocha(CABOCHA_PATH)
    val sentence = cabocha.executeAnalyzeAsFormatXML(word).get
    val result = for {
      chunk <- CabochaUtils.chainPos(sentence.chunks, List(PosDiv.NOUN))
      token <- chunk.tokens
    } yield token.base

    result.foreach(println)

    result

//    val tokenizer = Tokenizer.builder.mode(Tokenizer.Mode.NORMAL).build
//
//    val tokens = tokenizer.tokenize(word).toList
//
//    tokens.foreach(t => println(s"surface = ${t.getSurfaceForm}"))
//    tokens.map(_.getSurfaceForm)

//    val nouns = tokens.filter(nounFilter).map(createAnalyzedToken)
//
//    val verbs = tokens.filter(verbFilter).map(createAnalyzedToken)
//
//    (nouns, verbs)
  }

  private def getPos(featuresArray: Array[String]): String = featuresArray.getElemOrElse(0, "none")

  private def getCpos(featuresArray: Array[String]): String = featuresArray.getElemOrElse(1, "none")

  private def createAnalyzedToken(token: Token): AnalyzedToken = {
    val baseForm = token.getSurfaceForm
    val pos = getPos(token.getAllFeaturesArray)
    val cpos = getCpos(token.getAllFeaturesArray)
    val read = token.getReading
    new AnalyzedToken(baseForm, pos, cpos, read)
  }

  private def nounFilter(token: Token): Boolean = getPos(token.getAllFeaturesArray) match {
    case POS_NOUN => true
    case POS_PRONOUN => true
    case _ => false
  }

  private def verbFilter(token: Token): Boolean = getPos(token.getAllFeaturesArray) match {
    case POS_VERB => true
    case _ => false
  }

}
