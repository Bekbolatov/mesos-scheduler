package com.sparkydots.mesos.example

import org.apache.mesos.ExecutorDriver
import org.apache.mesos.Protos.TaskInfo

/**
 * Some executor messages
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/27/14 2:46 PM
 */
object ExecutorMessages {

  abstract class Message

  case class SimpleMessage(message: String) extends Message

  case class TaskInfoMessage(driver: ExecutorDriver, taskInfo: TaskInfo) extends Message

}
