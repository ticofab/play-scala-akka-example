package actors

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import consts.Consts
import controllers.Application
import messages._

class BossActor extends Actor {
  var clientIdCounter = 0

  override def receive: Receive = {

    case ClientConnected =>
      // creates a new WorkerActor and tell him to start doing some work
      val newWorker = ActorSystem().actorOf(Props[WorkerActor])
      newWorker ! StartWorking(clientIdCounter)
      sender ! clientIdCounter
      clientIdCounter = clientIdCounter + 1

    case WorkCycleCompleted(clientId, cycle) =>
      // forward the information to the corresponding message actor
      Application.messengerActor ! WorkCycleCompleted(clientId, cycle)
      if (cycle == Consts.totalWorkCycle) {
        // this actor is done with his work
        sender ! PoisonPill
      }

  }
}
