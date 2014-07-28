package com.sparkydots.mesos.framework.scheduler.akka

import java.util

import akka.actor.ActorRef
import com.sparkydots.mesos.framework.logging.LoggingMesosScheduler
import com.sparkydots.mesos.framework.scheduler.akka.SchedulerMessages._
import org.apache.mesos.Protos.{FrameworkID, MasterInfo, Offer, TaskStatus}
import org.apache.mesos.SchedulerDriver

import scala.collection.JavaConverters._

/**
 * Passing through message to SchedulerActor, which allows for easier synchronization with new task additions
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/26/14 9:32 PM
 */
class PassthruMesosScheduler(actor: ActorRef) extends LoggingMesosScheduler {

  override def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo) {
    super.registered(driver, frameworkId, masterInfo)
    actor ! Registered(frameworkId, masterInfo)
  }

  override def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo) {
    super.reregistered(driver, masterInfo)
    actor ! Reregistered(masterInfo)
  }

  override def resourceOffers(driver: SchedulerDriver, offers: util.List[Offer]) {
    super.resourceOffers(driver, offers)
    actor ! ResourceOffers(driver, offers.asScala.toList)
  }

  override def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    super.statusUpdate(driver, status)
    actor ! StatusUpdate(driver, status)
  }
}
