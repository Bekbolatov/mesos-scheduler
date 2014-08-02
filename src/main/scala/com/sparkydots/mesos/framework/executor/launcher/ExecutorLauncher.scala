package com.sparkydots.mesos.framework.executor.launcher

import com.sparkydots.mesos.framework.executor.basic.BasicExecutor
import org.apache.mesos.MesosExecutorDriver
import org.apache.mesos.Protos.Status
import xsbti.AppMain

/**
 * @author Renat Bekbolatov (renatb@sparkydots.com) 8/1/14 9:03 PM
 */


class ExecutorLauncher extends AppMain {
  def run(configuration: xsbti.AppConfiguration) = {

    println("Launching executor...")

    val mesosExecutorDriver: MesosExecutorDriver = new MesosExecutorDriver(new BasicExecutor)
    val driverStatus = mesosExecutorDriver.run()
    val exitStatus = if (driverStatus == Status.DRIVER_STOPPED) 0 else 1

    println("Executor exiting...")

    new Exit(exitStatus)
  }

  class Exit(val code: Int) extends xsbti.Exit

}