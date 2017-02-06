package actors

import java.util.Calendar
import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import akka.event.LoggingReceive
import com.google.inject.assistedinject.Assisted
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class PingActor @Inject()(@Assisted hub: ActorRef) extends Actor with ActorLogging {
  import PingActor._
  private var scheduler: Cancellable = _

  //TODO: https://github.com/pvillega/play21-redis-pubsub/blob/d22c3372a74bd73c904f77217fc051dbd8c7c57b/app/controllers/Application.scala

  import scala.concurrent.duration._
  scheduler = context.system.scheduler.schedule(
    initialDelay = 3 seconds,
    interval = 10 seconds,
    receiver = self,
    message = Tick()
  )

  def receive = LoggingReceive {
    case tick:Tick =>
      hub ! HubActor.Broadcast(Json.obj(
        "tick" -> "This is tick",
        "created_at" -> Calendar.getInstance().getTime()
      ))
    case _ => println("This is PingActor, whatever.")
  }

  override def postStop(): Unit = { scheduler.cancel() }
}

object PingActor {
  sealed case class Tick()
}