package com.sparkydots.mesos.framework.scheduler.akka

import org.apache.mesos.Protos.{FrameworkID, FrameworkInfo, Status}
import org.apache.mesos.{MesosSchedulerDriver, Scheduler => MesosScheduler}

/**
 * Holds a reference to our scheduler driver and manages start/stop and re-connection/failover in multi-threaded setting
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/27/14 1:21 PM
 */
class DriverManager {
  var _frameworkInfo: Option[FrameworkInfo] = None
  var _masterAddress: Option[String] = None
  var _schedulerDriver: Option[MesosSchedulerDriver] = None

  def init(scheduler: MesosScheduler, frameworkInfo: FrameworkInfo, masterAddress: String) {
    stop()

    _frameworkInfo = Some(frameworkInfo)
    _masterAddress = Some(masterAddress)
    _schedulerDriver = Some(new MesosSchedulerDriver(scheduler, _frameworkInfo.get, _masterAddress.get))

    run()
  }

  def reinit(scheduler: MesosScheduler, frameworkId: FrameworkID, masterAddress: Option[String]) {
    stop()

    _frameworkInfo = Some(FrameworkInfo.newBuilder(_frameworkInfo.get).setId(frameworkId).build())
    _masterAddress = masterAddress orElse _masterAddress
    _schedulerDriver = Some(new MesosSchedulerDriver(scheduler, _frameworkInfo.get, _masterAddress.get))

    run()
  }

  private def run() {
    if (_schedulerDriver.nonEmpty) {
      new Thread() {
        override def run() {
          _schedulerDriver.get.run()
        }
      }.start()
    }
  }

  def stop(failover: Boolean = false): Option[Status] = {
    if (_schedulerDriver.nonEmpty) {
      val status = Some(_schedulerDriver.get.stop(failover))
      _schedulerDriver = None
      status
    } else {
      None
    }
  }
}
