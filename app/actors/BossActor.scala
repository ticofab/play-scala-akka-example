package actors

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.pattern.ask
import consts.Consts
import controllers.Application.WSLink
import messages.{ClientConnected, ClientRegistersForMessages, StartWorking, WorkCycleCompleted}

import scala.concurrent.ExecutionContext.Implicits.global

class BossActor extends Actor {

  val messengerActor = ActorSystem().actorOf(Props[WebsocketMessageActor])
  var clientIdCounter = 0

  override def receive: Receive = {

    case ClientConnected =>
      // creates a new WorkerActor and tell him to start doing some work
      val newWorker = ActorSystem().actorOf(Props[WorkerActor])
      newWorker ! StartWorking(clientIdCounter)
      sender ! clientIdCounter
      clientIdCounter = clientIdCounter + 1

    case ClientRegistersForMessages(clientId) =>
      // asks the messenger actor for a websocket link
      val futureWSLink = (messengerActor ? ClientRegistersForMessages(clientId))(Consts.askTimeout).mapTo[WSLink]
      futureWSLink map {
        case (in, out) => sender !(in, out)
      }

    case WorkCycleCompleted(clientId, cycle) =>
      // forward the information to the corresponding message actor
      messengerActor ! WorkCycleCompleted(clientId, cycle)
      if (cycle == Consts.totalWorkCycle) {
        // this actor is done with his work
        sender ! PoisonPill
      }

  }
}
