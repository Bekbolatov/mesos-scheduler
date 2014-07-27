package com.sparkydots.mesos.framework

import org.apache.mesos.Protos.{TaskStatus, ExecutorInfo, Offer}
import org.apache.mesos.SchedulerDriver

/**
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/26/14 9:12 PM
 */
package object scheduler {

  abstract class SchedulerMessage

  case class NewTask(taskDescription: String, id: String) extends SchedulerMessage
  case class NewExecutorInfo(executorInfo: ExecutorInfo) extends SchedulerMessage

  case class ResourceOffers(driver: SchedulerDriver, offers: List[Offer]) extends SchedulerMessage
  case class StatusUpdate(driver: SchedulerDriver, status: TaskStatus) extends SchedulerMessage
}
