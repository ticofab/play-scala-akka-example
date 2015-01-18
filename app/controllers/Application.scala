package controllers

import actors.BossActor
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import messages.ClientRegistersForMessages
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.Future

object Application extends Controller {
  type WSLink = (Iteratee[String, _], Enumerator[String])

  val bossActor = ActorSystem().actorOf(Props[BossActor])

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def openWebsocket(clientId: Int) = WebSocket.tryAccept[String] {
    request =>
      val wsLinkFuture: Future[WSLink] = (bossActor ? ClientRegistersForMessages(clientId)).mapTo[WSLink]
      wsLinkFuture map {
        case x: WSLink => Right(x)
        case _ => Left(NotFound)
      }

  }
}
