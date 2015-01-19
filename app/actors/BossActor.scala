package actors

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import consts.Consts
import controllers.Application
import messages._

/**
 * This is the supervisor actor that will spawn the worker actors at each client connection.
 * Anytime that the worker actors send their updates, this guy communicates the change to the
 * messenger actor.
 */
class BossActor extends Actor {
  var clientIdCounter = 0
  var activeClients = 0

  override def receive: Receive = {

    // A new client has connected. Let's spawn a new WorkerActor and tell him to start working.
    case ClientConnected =>
      // creates a new WorkerActor and tell him to start doing some work
      val newWorker = ActorSystem().actorOf(Props[WorkerActor])
      clientIdCounter = clientIdCounter + 1
      activeClients = activeClients + 1
      newWorker ! StartWorking(clientIdCounter)

      // send a reply to the responding actor and update the messenger actor
      sender ! ClientConnectionSuccessful(clientIdCounter, activeClients)
      Application.messengerActor ! ActiveClientsChanged(activeClients)

    // A worker has completed some work
    case WorkCycleCompleted(clientId, cycle) =>

      // forward the information to the corresponding message actor
      Application.messengerActor ! WorkCycleCompleted(clientId, cycle)

      // is this actor done with his work?
      if (cycle == Consts.totalWorkCycle) {
        // terminate it
        sender ! PoisonPill

        // update the messenger actor
        activeClients = activeClients - 1
        Application.messengerActor ! ActiveClientsChanged(activeClients)

        // reset the number of clients if none is active anymore
        if (activeClients == 0) clientIdCounter = 0
      }

  }
}
