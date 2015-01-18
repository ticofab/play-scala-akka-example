package actors

import akka.actor.Actor
import controllers.Application.WSLink
import messages.{ClientRegistersForMessages, WorkCycleCompleted}
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.{Concurrent, Iteratee}

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global

class WebsocketMessageActor extends Actor {
  var clientMessengerMap = ListMap.empty[Int, Channel[String]]

  override def receive: Receive = {
    case ClientRegistersForMessages(clientId) =>
      val wsLink: WSLink = (
        // ignore anything that comes in
        Iteratee.ignore[String],

        // channel to push chunks to the client
        Concurrent.unicast(channel => {
          // onStart, creates a link between the client id and the channel
          clientMessengerMap = clientMessengerMap + (clientId -> channel)
        }))

      // return the websocket link
      sender ! wsLink

    case WorkCycleCompleted(clientId, cycle) =>
      // update client with the cycle number
      clientMessengerMap.get(clientId).foreach(c => c.push(cycle.toString))
  }
}
