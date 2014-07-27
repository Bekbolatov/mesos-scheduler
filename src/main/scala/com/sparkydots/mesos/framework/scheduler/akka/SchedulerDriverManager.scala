package com.sparkydots.mesos.framework.scheduler.akka

import org.apache.mesos.Protos.{FrameworkInfo, Status}
import org.apache.mesos.{MesosSchedulerDriver, Scheduler}

/**
 * Holds a reference to our scheduler driver and manages start/stop and re-connection/failover in multi-threaded setting
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/27/14 1:21 PM
 */
class SchedulerDriverManager {
  var schedulerDriver: Option[MesosSchedulerDriver] = None

  def init(scheduler: Scheduler, frameworkInfo: FrameworkInfo, masterAddress: String) {
    if (schedulerDriver.nonEmpty) {
      schedulerDriver.get.stop()
    }
    schedulerDriver = Some(new MesosSchedulerDriver(scheduler, frameworkInfo, masterAddress))
  }

  def run() {
    if (schedulerDriver.nonEmpty) {
      new Thread() {
        override def run() {
          schedulerDriver.get.run()
        }
      }.start()
    }
  }

  def stop(failover: Boolean = false): Option[Status] = {
    if (schedulerDriver.nonEmpty) {
      Some(schedulerDriver.get.stop(failover))
    } else {
      None
    }
  }
}
