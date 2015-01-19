package actors

import akka.actor.Actor
import controllers.Application.WSLink
import messages.{ActiveClientsChanged, ClientRegistersForActiveClients, ClientRegistersForWorkCycles, WorkCycleCompleted}
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.{Concurrent, Iteratee}

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This actor updates clients in real time about the status of their work and
 * about the total amount of clients connected.
 */
class WebsocketMessageActor extends Actor {

  // This map links a client with push channel
  var clientMessengerMap = ListMap.empty[Int, Channel[String]]

  // This is the broadcast couple - enumerator and channel to push chunks down it.
  val (feedEnumerator, feedChannel) = Concurrent.broadcast[String]

  override def receive: Receive = {

    // A client registers for updates on its work cycles.
    case ClientRegistersForWorkCycles(clientId) =>
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
      
    // A client registers for information about the active clients.
    case ClientRegistersForActiveClients =>
      val wsLink = (Iteratee.ignore, feedEnumerator)
      sender ! wsLink

    // One of the workers has completed one work cycle.
    case WorkCycleCompleted(clientId, cycle) =>
      // update client with the cycle number
      clientMessengerMap.get(clientId).foreach(c => c.push(cycle.toString))

    // The total number of active clients has changed.
    case ActiveClientsChanged(activeClients) =>
      feedChannel.push(activeClients.toString)
  }
}
