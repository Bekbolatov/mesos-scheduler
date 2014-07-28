package com.sparkydots.mesos.framework.scheduler.akka

import org.apache.mesos.Protos._
import org.apache.mesos.SchedulerDriver

/**
 * Messages that we will use to communicate with Scheduler
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/27/14 3:11 PM
 */
object SchedulerMessages {

  abstract class SchedulerMessage


  // original MesosScheduler calls
  case class Registered(frameworkID: FrameworkID, masterInfo: MasterInfo) extends SchedulerMessage

  case class Reregistered(masterInfo: MasterInfo) extends SchedulerMessage

  case class ResourceOffers(driver: SchedulerDriver, offers: List[Offer]) extends SchedulerMessage

  case class StatusUpdate(driver: SchedulerDriver, status: TaskStatus) extends SchedulerMessage

  // messages communicated by runner
  case class Initialize(executorInfo: ExecutorInfo, frameworkName: String, masterAddress: String) extends SchedulerMessage

  case class Reinitialize(masterAddress: Option[String] = None) extends SchedulerMessage

  case class StopDriver(failover: Boolean = false) extends SchedulerMessage

  case class SetMaxTries(newMaxTries: Int) extends SchedulerMessage

  case class NewTaskRequest(taskDescription: String, id: String) extends SchedulerMessage

}
