package actors

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.event.LoggingReceive

import scala.concurrent.ExecutionContext

@Singleton
class HubActor @Inject()(implicit ec: ExecutionContext) extends Actor with ActorLogging {
  import HubActor._
  var connections = Set.empty[ActorRef]

  override def receive = LoggingReceive {
    case ConnectionMade() =>
      connections += sender
      context watch sender

    case ConnectionClosed() =>
      connections = connections.filterNot((actor:ActorRef) => actor == sender)

    case broadcast:Broadcast =>
      connections.par.foreach(_ ! broadcast)

    case _ => println("HubActor got something strange. Ignoring.")
  }
}

object HubActor {
  sealed case class ConnectionMade()
  sealed case class ConnectionClosed()
  sealed case class Broadcast(something: AnyRef)
  sealed case class BroadcastToOthers(something: AnyRef)
}