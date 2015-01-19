package actors

import akka.actor.Actor
import consts.Consts
import messages.{StartWorking, WorkCycleCompleted}
import play.api.Logger

class WorkerActor extends Actor {
  override def receive: Receive = {

    case StartWorking(clientId) =>
      for (cycle <- 1 to Consts.totalWorkCycle) {
        Thread.sleep(1000)
        Logger.debug("worker for client " + clientId + ", completed cycle " + cycle)
        sender ! WorkCycleCompleted(clientId, cycle)
      }

  }
}
