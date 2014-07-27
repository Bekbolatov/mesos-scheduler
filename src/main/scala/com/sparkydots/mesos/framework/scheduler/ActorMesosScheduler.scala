package com.sparkydots.mesos.framework.scheduler

import java.util

import akka.actor.ActorRef
import org.apache.mesos.Protos.{TaskStatus, Offer}
import org.apache.mesos.SchedulerDriver

import scala.collection.JavaConverters._

/**
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
