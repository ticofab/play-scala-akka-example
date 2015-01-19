package actors

import akka.actor.Actor
import consts.Consts
import messages.{StartWorking, WorkCycleCompleted}

/**
 * One worker actor is spawned for each client connecting. This actor will complete
 * the work cycles and send an update for each work cycle completed.
 */
class WorkerActor extends Actor {
  override def receive: Receive = {

    case StartWorking(clientId) =>
      for (cycle <- 1 to Consts.totalWorkCycle) {
        Thread.sleep(1000)
        sender ! WorkCycleCompleted(clientId, cycle)
      }

  }
}
