package boot

import akka.actor.{Props, ActorSystem}
import akka.io.IO
import config.Configuration
import rest.RestServiceActor
import spray.can.Http

object Boot extends App with Configuration {
  implicit val system = ActorSystem("spray-server-template")

  val restService = system.actorOf(Props[RestServiceActor], "rest-endpoint")

  IO(Http) ! Http.Bind(restService, serviceHost, servicePort)
}
