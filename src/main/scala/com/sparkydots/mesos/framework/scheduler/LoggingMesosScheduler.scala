package com.sparkydots.mesos.framework.scheduler

import java.util

import com.typesafe.scalalogging.LazyLogging
import org.apache.mesos.Protos._
import org.apache.mesos.{Scheduler, SchedulerDriver}

trait LoggingMesosScheduler extends Scheduler with LazyLogging {

  def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo) {
    logger info "===================== registered ==============================="
    logger info s". frameworkId = $frameworkId"
    logger info s". masterInfo = $masterInfo"
  }

  def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo) {
    logger info "=============================== reregistered ==============================="
    logger info s". masterInfo = $masterInfo"
  }

  def resourceOffers(driver: SchedulerDriver, offers: util.List[Offer]) {
    logger info "=============================== resourceOffers ==============================="
    logger info s". number of offers = ${offers.size()}"
  }

  def offerRescinded(driver: SchedulerDriver, offerId: OfferID) {
    logger info "=============================== offerRescinded ==============================="
    logger info s". offerId = $offerId"
  }

  def disconnected(driver: SchedulerDriver) {
    logger info "=============================== disconnected ==============================="
  }



  def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]) {
    logger info "=============================== frameworkMessage ==============================="
    logger info s". executorId = $executorId"
    logger info s". slaveId = $slaveId"
  }

  def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    logger info "=============================== statusUpdate ==============================="
    logger info s". status = $status"
  }



  def slaveLost(driver: SchedulerDriver, slaveId: SlaveID) {
    logger info "=============================== slaveLost ==============================="
    logger info s". slaveId = $slaveId"
  }

  def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int) {
    logger info "=============================== executorLost ==============================="
    logger info s". executorId = $executorId"
    logger info s". slaveId = $slaveId"
    logger info s". status = $status"
  }

  def error(driver: SchedulerDriver, message: String) {
    logger info "=============================== error ==============================="
    logger info s". message = $message"
  }

}
