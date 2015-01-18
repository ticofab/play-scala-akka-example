package actors

import akka.actor.Actor
import messages.{StartWorking, WorkCycleCompleted}

class WorkerActor extends Actor {
  override def receive: Receive = {

    case StartWorking(clientId) =>
      for (cycle <- 1 to 100) {
        Thread.sleep(1000)
        context.parent ! WorkCycleCompleted(clientId, cycle)
      }

  }
}
