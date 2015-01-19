package actors

import akka.actor.Actor
import consts.Consts
import messages.{StartWorking, WorkCycleCompleted}

class WorkerActor extends Actor {
  override def receive: Receive = {

    case StartWorking(clientId) =>
      for (cycle <- 1 to Consts.totalWorkCycle) {
        Thread.sleep(1000)
        sender ! WorkCycleCompleted(clientId, cycle)
      }

  }
}
