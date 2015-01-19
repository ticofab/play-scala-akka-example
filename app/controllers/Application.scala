package controllers

import actors.{BossActor, WebsocketMessageActor}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import consts.Consts
import messages.{ClientRegistersForActiveClients, ClientConnected, ClientRegistersForMessages}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Application extends Controller {
  type WSLink = (Iteratee[String, _], Enumerator[String])

  val messengerActor = ActorSystem().actorOf(Props[WebsocketMessageActor])
  val bossActor = ActorSystem().actorOf(Props[BossActor])

  def newClient = Action.async {
    val futureId = (bossActor ? ClientConnected)(Consts.askTimeout).mapTo[Int]
    futureId.map(id => Ok(views.html.index(id.toString, Consts.totalWorkCycle.toString)))
  }

  def getCycles(clientId: Int) = WebSocket.tryAccept[String] {
    request =>
      val futureWSLink: Future[WSLink] = (messengerActor ? ClientRegistersForMessages(clientId))(Consts.askTimeout).mapTo[WSLink]
      futureWSLink map {
        case x: WSLink => Right(x)
        case _ => Left(NotFound)
      }
  }

  def getActive = WebSocket.tryAccept[String] {
    request =>
      val futureWSLink: Future[WSLink] = (messengerActor ? ClientRegistersForActiveClients)(Consts.askTimeout).mapTo[WSLink]
      futureWSLink map {
        case x: WSLink => Right(x)
        case _ => Left(NotFound)
      }
  }
}
