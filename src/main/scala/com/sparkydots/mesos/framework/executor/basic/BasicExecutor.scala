package com.sparkydots.mesos.framework.executor.basic

import akka.actor.{ActorSystem, Props}
import ExecutorMessages._
import com.sparkydots.mesos.framework.executor.ExecutorActor
import com.sparkydots.mesos.framework.logging.LoggingMesosExecutor
import org.apache.mesos.ExecutorDriver
import org.apache.mesos.Protos._

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

