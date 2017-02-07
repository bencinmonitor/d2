package actors

import javax.inject.Inject
import com.google.inject.assistedinject.Assisted
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import play.api.libs.json.Json

class WebSocketActor @Inject()(@Assisted out: ActorRef, @Assisted hub: ActorRef) extends Actor with ActorLogging {

  import play.api.libs.json.JsValue

  override def preStart(): Unit = hub ! HubActor.ConnectionMade()

  override def receive = LoggingReceive {
    case msg: JsValue =>
      log.debug("I got message via websocket,...")
      out ! Json.obj("got" -> msg)
      hub ! HubActor.Broadcast(Json.obj("someone_wrote" -> msg))
    case HubActor.Broadcast(msg: JsValue) =>
      out ! msg
  }

  override def postStop() = hub ! HubActor.ConnectionClosed()
}

