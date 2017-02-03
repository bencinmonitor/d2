import com.google.inject.AbstractModule
import com.google.inject.name.{Named, Names}
import javax.inject.{Inject, Named}

import akka.actor.Actor
import akka.routing.RoundRobinPool
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  import actors._

  override def configure() = {
    // bind(classOf[ApplicationService]).asEagerSingleton()
    // bind(classOf[SetupService]).asEagerSingleton()
    // bind(classOf[ScheduleService]).asEagerSingleton()
    // actors:
    // bindActor[CurrenciesActor]("currenciesActor")

    bindActor[HubActor]("hubActor")

    // bind(classOf[HubActor]).annotatedWith(Names.named("hubActor")).asEagerSingleton()
    // bind(classOf[Actor]).annotatedWith(Names.named("hubActor")).to(HubActor)
    // bind(classOf[HubActor]).asEagerSingleton()
  }
}