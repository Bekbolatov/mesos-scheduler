package com.sparkydots.mesos.framework.scheduler

import org.apache.mesos.Protos.{ExecutorInfo, Offer, TaskStatus}
import org.apache.mesos.SchedulerDriver

/**
 * SchedulerActor communication
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/26/14 9:12 PM
 */
package object akka {
  abstract class SchedulerMessage

  case class Initialize(executorInfo: ExecutorInfo, frameworkName: String, masterAddress: String) extends SchedulerMessage
  case class StopDriver(failover: Boolean = false) extends SchedulerMessage
  case class SetMaxTries(newMaxTries: Int) extends SchedulerMessage

  case class NewTask(taskDescription: String, id: String) extends SchedulerMessage
  case class ResourceOffers(driver: SchedulerDriver, offers: List[Offer]) extends SchedulerMessage
  case class StatusUpdate(driver: SchedulerDriver, status: TaskStatus) extends SchedulerMessage
}
