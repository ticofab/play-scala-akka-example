package controllers

import actors.{BossActor, WebsocketMessageActor}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import consts.Consts
import messages.{ClientConnected, ClientRegistersForActiveClients, ClientRegistersForMessages}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Application extends Controller {
  type WSLink = (Iteratee[String, _], Enumerator[String])

  val messengerActor = ActorSystem().actorOf(Props[WebsocketMessageActor])
  val bossActor = ActorSystem().actorOf(Props[BossActor])

  def newClient = Action.async {
    val futureStr = (bossActor ? ClientConnected)(Consts.askTimeout).mapTo[String]
    futureStr.map(str => {
      val sepIndex = str.indexOf(',')
      val id = str.substring(0, sepIndex)
      val activeClients = str.substring(sepIndex + 1)
      Ok(views.html.index(id, activeClients, Consts.totalWorkCycle.toString))
    })
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
