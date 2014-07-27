package com.sparkydots.mesos.framework.executor

import akka.actor.Actor
import com.sparkydots.mesos.example.{SimpleMessage, TaskInfoMessage}
import com.typesafe.scalalogging.LazyLogging
import org.apache.mesos.Protos.{TaskInfo, TaskState, TaskStatus}

/**
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/26/14 9:14 PM
 */
class ExecutorActor extends Actor with LazyLogging {

  def taskStatus(taskInfo: TaskInfo, state: TaskState) =
    TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId).setState(state).build()

  def receive: Receive = {

    case SimpleMessage(message) =>
      logger info s"Received SimpleMessage: $message"

    case TaskInfoMessage(driver, taskInfo) =>
      logger info s"Received TaskInfoMessage with taskInfo: $taskInfo"
      logger info s"and driver: $driver"

      driver.sendStatusUpdate(taskStatus(taskInfo, TaskState.TASK_RUNNING))
      logger info "*|*|*|* :  RUNNING HERE"
      driver.sendStatusUpdate(taskStatus(taskInfo, TaskState.TASK_FINISHED))

    case _ =>
      logger info "Received unknown message"
  }
}
