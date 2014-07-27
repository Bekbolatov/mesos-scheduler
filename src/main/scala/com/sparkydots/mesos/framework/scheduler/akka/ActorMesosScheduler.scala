package com.sparkydots.mesos.framework.scheduler.akka

import java.util

import akka.actor.ActorRef
import com.sparkydots.mesos.framework.logging.LoggingMesosScheduler
import org.apache.mesos.Protos.{Offer, TaskStatus}
import org.apache.mesos.SchedulerDriver

import scala.collection.JavaConverters._

/**
 * Passing through message to SchedulerActor, which allows for easier synchronization with new task additions
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/26/14 9:32 PM
 */
class ActorMesosScheduler(actor: ActorRef) extends LoggingMesosScheduler {

  override def resourceOffers(driver: SchedulerDriver, offers: util.List[Offer]) {
    super.resourceOffers(driver, offers)
    actor ! ResourceOffers(driver, offers.asScala.toList)
  }

  override def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    super.statusUpdate(driver, status)
    actor ! StatusUpdate(driver, status)
  }
}
