package actors

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.pattern.ask
import controllers.Application.WSLink
import messages.{ClientConnected, ClientRegistersForMessages, StartWorking, WorkCycleCompleted}

class BossActor extends Actor {

  val messengerActor = ActorSystem().actorOf(Props[WebsocketMessageActor])
  var clientIdCounter = 0

  override def receive: Receive = {

    case ClientConnected =>
      // creates a new WorkerActor and tell him to start doing some work
      val newWorker = ActorSystem().actorOf(Props[WorkerActor])
      newWorker ! StartWorking(clientIdCounter)
      clientIdCounter = clientIdCounter + 1

    case ClientRegistersForMessages(clientId) =>
      // asks the messenger actor for a websocket link
      val wsFuture = (messengerActor ? ClientRegistersForMessages(clientId)).mapTo[WSLink]
      wsFuture map {
        case (in, out) => sender !(in, out)
      }

    case WorkCycleCompleted(clientId, cycle) =>
      // forward the information to the corresponding message actor
      messengerActor ! WorkCycleCompleted(clientId, cycle)
      if (cycle == 100) {
        // this actor is done with his work
        sender ! PoisonPill
      }

  }
}
