package com.sparkydots.mesos.example

import akka.actor.{ActorSystem, Props}
import com.sparkydots.mesos.example.ExecutorMessages._
import com.sparkydots.mesos.framework.executor.ExecutorActor
import com.sparkydots.mesos.framework.logging.LoggingMesosExecutor
import org.apache.mesos.Protos._
import org.apache.mesos.{ExecutorDriver, MesosExecutorDriver}

class BasicExecutor(poolSize: Int = 5) extends LoggingMesosExecutor {

  val system = ActorSystem("ExecutorSystem")
  val executorActor = system.actorOf(Props[ExecutorActor], "executorActor")

  override def launchTask(driver: ExecutorDriver, task: TaskInfo) {
    super.launchTask(driver, task)
    executorActor ! SimpleMessage("helo")
    executorActor ! TaskInfoMessage(driver, task)
  }

  override def shutdown(driver: ExecutorDriver) {
    system.shutdown()
  }

}


object BasicExecutor {
  def main(args: Array[String]) {
    val mesosExecutorDriver: MesosExecutorDriver = new MesosExecutorDriver(new BasicExecutor)
    val driverStatus = mesosExecutorDriver.run()
    val exitStatus = if (driverStatus == Status.DRIVER_STOPPED) 0 else 1
    System.exit(exitStatus)
  }
}
