package controllers

import actors.{BossActor, WebsocketMessageActor}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import consts.Consts
import messages.{ClientConnected, ClientConnectionSuccessful, ClientRegistersForActiveClients, ClientRegistersForWorkCycles}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Application extends Controller {
  type WSLink = (Iteratee[String, _], Enumerator[String])

  val messengerActor = ActorSystem().actorOf(Props[WebsocketMessageActor])
  val bossActor = ActorSystem().actorOf(Props[BossActor])

  /**
   * Entry point for clients.
   * @return An Action carryng the main HTML view.
   */
  def newClient = Action.async {
    val futureStr = (bossActor ? ClientConnected)(Consts.askTimeout).mapTo[ClientConnectionSuccessful]
    futureStr.map(msg => Ok(views.html.index(msg.id, msg.activeClients, Consts.totalWorkCycle)))
  }

  /**
   * Websocket endpoint to update in real time the number of work cycles completed.
   * @param clientId  The client's id, as returned from opening a connection through newClient.
   * @return  The Websocket that will update the client on the number of completed cycles.
   */
  def getCycles(clientId: Int) = WebSocket.tryAccept[String] {
    request =>
      val futureWSLink: Future[WSLink] = (messengerActor ? ClientRegistersForWorkCycles(clientId))(Consts.askTimeout).mapTo[WSLink]
      futureWSLink map {
        case x: WSLink => Right(x)
        case _ => Left(NotFound)
      }
  }

  /**
   * Websocket endpoint that broadcast to all connected clients the total amount of connected clients.
   * @return  A Websocket endpoint.
   */
  def getActive = WebSocket.tryAccept[String] {
    request =>
      val futureWSLink: Future[WSLink] = (messengerActor ? ClientRegistersForActiveClients)(Consts.askTimeout).mapTo[WSLink]
      futureWSLink map {
        case x: WSLink => Right(x)
        case _ => Left(NotFound)
      }
  }
}
