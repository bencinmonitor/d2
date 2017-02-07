package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import play.api._
import play.api.libs.json._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import libs.concurrent.Akka
import play.api.mvc._
import akka.util.Timeout
import java.util.concurrent.TimeUnit._
import java.lang.String

import org.sedis.Pool
import javax.inject._

import com.google.inject.assistedinject.Assisted

import concurrent.duration.Duration
import redis.clients.jedis.JedisPubSub

import concurrent.Future
import scala.concurrent.ExecutionContext
import org.sedis.{Pool => RedisPool, _}

class RedisEventsActor @Inject()(@Named("hubActor") hub: ActorRef, val redis: RedisPool)(implicit executionContext: ExecutionContext) extends Actor with ActorLogging {

  import RedisEventsActor._

  final var PATTERN: String = "events.*"

  Future {
    redis.withJedisClient { implicit client => client.psubscribe(new Listener(this), PATTERN) }
  }(executionContext)

  def receive = LoggingReceive {
    case PMessage(_, _, message) => hub ! HubActor.Broadcast(message)
    case Message(_, message) => hub ! HubActor.Broadcast(message)
  }

  class Listener(val redisEventsActor: RedisEventsActor) extends JedisPubSub {

    import RedisEventsActor._

    @Inject
    @Named val hub: ActorRef = null

    def onMessage(channel: String, message: String): Unit = {
      redisEventsActor.self ! Message(channel, message)
    }

    def onPMessage(pattern: String, channel: String, message: String): Unit = {
      redisEventsActor.self ! PMessage(pattern, channel, message)
    }

    def onSubscribe(channel: String, subscribedChannels: Int): Unit = {
      Logger.info("onSubscribe[%s, %d]".format(channel, subscribedChannels))
    }

    def onUnsubscribe(channel: String, subscribedChannels: Int): Unit = {
      Logger.info("onUnsubscribe[%s, %d]".format(channel, subscribedChannels))
    }

    def onPSubscribe(pattern: String, subscribedChannels: Int): Unit = {
      Logger.info("onPSubscribe[%s, %d]".format(pattern, subscribedChannels))
    }

    def onPUnsubscribe(pattern: String, subscribedChannels: Int): Unit = {
      Logger.info("onPUnsubscribe[%s, %d]".format(pattern, subscribedChannels))
    }
  }

}


object RedisEventsActor {

  sealed case class Message(channel: String, message: JsValue)

  object Message {
    def apply(channel: String, message: String): Message = Message(channel, Json.parse(message))
  }

  sealed case class PMessage(pattern: String, channel: String, message: JsValue)

  object PMessage {
    def apply(pattern: String, channel: String, message: String): PMessage = new PMessage(pattern, channel, Json.parse(message))
  }

}