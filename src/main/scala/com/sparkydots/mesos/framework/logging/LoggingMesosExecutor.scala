package com.sparkydots.mesos.framework.logging

import com.typesafe.scalalogging.LazyLogging
import org.apache.mesos.Protos._
import org.apache.mesos.{Executor, ExecutorDriver}

/**
 * Executor with logging pre-wired
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/27/14 12:56 PM
 */
trait LoggingMesosExecutor extends Executor with LazyLogging {


  def registered(driver: ExecutorDriver, executorInfo: ExecutorInfo, frameworkInfo: FrameworkInfo, slaveInfo: SlaveInfo) {
    logger info "*** [registered] *** "
    logger info "executorInfo:"
    logger info s"$executorInfo"
    logger info "frameworkInfo:"
    logger info s"$frameworkInfo"
    logger info "slaveInfo:"
    logger info s"$slaveInfo"
  }

  def shutdown(driver: ExecutorDriver) {
    logger info "*** [shutdown] ***"
  }

  def disconnected(driver: ExecutorDriver) {
    logger info "*** [disconnected] ***"
  }

  def killTask(driver: ExecutorDriver, taskId: TaskID) {
    logger info "*** [killTask] ***"
    logger info "taskId:"
    logger info s"$taskId"
  }

  def reregistered(driver: ExecutorDriver, slaveInfo: SlaveInfo) {
    logger info "*** [reregistered] ***"
    logger info "slaveInfo:"
    logger info s"$slaveInfo"
  }

  def error(driver: ExecutorDriver, message: String) {
    logger info "*** [error] ***"
    logger info "message:"
    logger info s"$message"

  }

  def frameworkMessage(driver: ExecutorDriver, data: Array[Byte]) {
    logger info "*** [frameworkMessage] ***"
    logger info "data:"
    logger info s"$data"

  }

  def launchTask(driver: ExecutorDriver, taskInfo: TaskInfo) {
    logger info "*** [launchTask] ***"
    logger info "task:"
    logger info s"$taskInfo"
  }
}
