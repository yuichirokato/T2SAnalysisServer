package rest

import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.Actor
import analysis.{Failure, MorphologicalAnalyzer, MusicSearch, Talk}
import net.liftweb.json.Serialization._
import net.liftweb.json.{DateFormat, Formats}
import spray.http._
import spray.httpx.unmarshalling._
import spray.routing._

import scala.util.control.Exception._

class RestServiceActor extends Actor with RestService {

  implicit def actorRefFactory = context

  override def receive = runRoute(rest)

}

trait RestService extends HttpService {

  implicit val liftJsonFormats = new Formats {
    override val dateFormat: DateFormat = new DateFormat {
      val sdf = new SimpleDateFormat("yyyy-MM-dd")

      override def parse(s: String): Option[Date] = catching(classOf[Exception]) opt sdf.parse(s)

      override def format(d: Date): String = sdf.format(d)
    }
  }

  implicit val customUnmarshaller = Unmarshaller[String](MediaTypes.`application/json`) {
    case httpEntity: HttpEntity => read[String](httpEntity.asString(HttpCharsets.`UTF-8`))
  }

  val rest = respondWithMediaType(MediaTypes.`application/json`) {
    path("analyze") {
      get {
        parameters('text.as[String], 'action_design.as[String]) { (analyzeText, ad) =>
          ctx: RequestContext => {
            handleRequest(ctx) {
              println(s"encodtext = $analyzeText")
              val decodeText = URLDecoder.decode(analyzeText, "UTF-8")
              val words = MorphologicalAnalyzer.analize(decodeText)

              ad match {
                case "default" => Talk.getTemplate(words)
                case _ => Talk.getTemplateWithAD(words, ad)
              }
            }
          }
        }
      }
    } ~
      path("music_search") {
        get {
          parameter('status.as[String]) { status =>
            ctx: RequestContext => {
              handleRequest(ctx) {
                MusicSearch.startSearch(status)
              }
            }
          }
        }
      }
  }

  protected def handleRequest(ctx: RequestContext, successCode: StatusCode = StatusCodes.OK)(action: => Either[Failure, _]) = {
    action match {
      case Right(result: Object) => ctx.complete(successCode, write(result))
      case Left(error: Failure) => ctx.complete(error.getStatusCode, write(Map("template" -> error.message)))
      case _ => ctx.complete(StatusCodes.InternalServerError)
    }
  }
}
