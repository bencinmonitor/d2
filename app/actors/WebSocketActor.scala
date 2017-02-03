package actors

import javax.inject.{Inject, Named}
import java.util.Calendar

import com.google.inject.assistedinject.Assisted
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import play.api.libs.json.Json

class WebSocketActor @Inject()(@Assisted out: ActorRef, @Assisted hub: ActorRef) extends Actor with ActorLogging {
  import play.api.libs.json.JsValue

  override def preStart(): Unit = hub ! HubActor.ConnectionMade()

  override def receive = LoggingReceive {
    case msg: JsValue =>
      log.debug("I got message via websocket,...")
      out ! Json.obj("got" -> msg)
      
      hub ! HubActor.Broadcast(Json.obj("somone_wrote" -> msg))

    // case BroadcastMessage(message) =>
    //  out ! Json.obj("event"->"time", "message" -> message, "created_at" -> Calendar.getInstance().getTime())

    case HubActor.Broadcast(msg: JsValue) =>
      out ! msg
    case _ =>
      out ! Json.toJson(Json.obj("error" -> "Sorry, only JSON!"))
  }

  override def postStop() = hub ! HubActor.ConnectionClosed()
}


/*
object MyWebSocketActor {
  var connections = 0
  def props(out: ActorRef) = {
    connections += 1
    Props(new MyWebSocketActor(out))
  }
}
*/

