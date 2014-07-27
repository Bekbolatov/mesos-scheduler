package com.sparkydots.mesos.framework.scheduler.akka

import akka.actor.Actor
import com.google.common.collect.Lists
import com.google.protobuf.ByteString
import com.typesafe.scalalogging.LazyLogging
import org.apache.mesos.Protos._
import org.apache.mesos.{MesosSchedulerDriver, Scheduler, SchedulerDriver}

import scala.collection.mutable

/**
 * Wraps MesosScheduler and the driver through Akka Actor, allowing for succinct task management
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/26/14 9:09 PM
 */

class SchedulerActor extends Actor with LazyLogging {

  def receive: Receive = {
    case Initialize(executorInfo, frameworkName, masterAddress) =>
      initialize(executorInfo, frameworkName, masterAddress)
    case StopDriver(failover) =>
      stopDriver(failover)
    case SetMaxTries(newMaxTries) =>
      maxTries = newMaxTries
    case NewTask(taskDescription, id) =>
      addNewTask(taskDescription, id)
    case ResourceOffers(driver, offers) =>
      processOffers(driver, offers)
    case StatusUpdate(driver, status) =>
      processStatusUpdate(driver, status)
    case _ =>
      logger warn "no message matched"
  }

  case class ActiveTask(taskInfo: TaskInfo, tries: Int, originalDescription: String)
  val taskQueue = new mutable.Queue[Tuple2[String, String]]
  val activeTasks = new mutable.HashMap[String, ActiveTask]

  var maxTries = 3
  var activeExecutor: Option[ExecutorInfo] = None
  var scheduler: Option[Scheduler] = None
  var frameworkInfo: Option[FrameworkInfo] = None
  val schedulerDriverManager = new SchedulerDriverManager




  def initialize(executorInfo: ExecutorInfo, frameworkName: String, masterAddress: String) {
    activeExecutor = Some(executorInfo)
    scheduler = Some(new ActorMesosScheduler(self))
    frameworkInfo = Some(FrameworkInfo.newBuilder().setUser("").setName(frameworkName).build())
    schedulerDriverManager.init(scheduler.get, frameworkInfo.get, masterAddress)
    schedulerDriverManager.run()
  }

  def stopDriver(failover: Boolean) {
    schedulerDriverManager.stop(failover)
  }

  def processStatusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    val taskId = status.getTaskId.getValue
    val commonTaskId = taskId.split("-")(0)
    val state = status.getState

    if (badState(state)) {
      val activeTaskLine = activeTasks.get(commonTaskId)
      if (activeTaskLine.isEmpty) {
        logger error "expected a record of previous task launch"
        activeTasks.remove(commonTaskId)
        return
      }
      if (activeTaskLine.get.tries < maxTries) {
        addNewTask(activeTaskLine.get.originalDescription, commonTaskId)
      } else {
        activeTasks.remove(commonTaskId)
        logger error("task {} could not complete after {} retries", commonTaskId, maxTries.toString)
      }
    } else if (state == TaskState.TASK_FINISHED) {
      activeTasks.remove(commonTaskId)
    } else {
      logger warn("unhandled task state message {}", state.toString)
    }
  }

  def badState(state: TaskState): Boolean = {
    state == TaskState.TASK_FAILED ||
      state == TaskState.TASK_KILLED ||
      state == TaskState.TASK_LOST
  }



  def addNewTask(taskDescription: String, baseTaskId: String) {
    taskQueue += Tuple2(taskDescription, baseTaskId)
  }

  def findSuitableTask(offer: Offer): Option[Tuple2[String, String]] = {
    if (taskQueue.isEmpty) {
      None
    } else {
      Some(taskQueue.dequeue())
    }
  }

  def makeNewTaskId(common: String): Option[Tuple2[String, Int]] = {
    val activeTask = activeTasks.get(common)
    if (activeTask.nonEmpty) {
      val count = activeTask.get.tries
      if (count > maxTries) {
        None
      } else {
        Some(Tuple2(common + "-" + (count + 1), count))
      }
    } else {
      Some(Tuple2(common + "-1", 0))
    }
  }

  def launchTask(driver: SchedulerDriver, offer: Offer, taskData: Tuple2[String, String]) {
    val newId = makeNewTaskId(taskData._2)
    if (newId.nonEmpty) {
      val taskInfo = createTask(offer, activeExecutor.get, newId.get._1, taskData._1)
      val filters = Filters.newBuilder().setRefuseSeconds(1).build()
      driver.launchTasks(Lists.newArrayList(offer.getId), Lists.newArrayList(taskInfo), filters)

      activeTasks += (taskInfo.getTaskId.getValue -> ActiveTask(taskInfo, newId.get._2 + 1, taskData._1))
      logger info("launching task {}", newId)
    } else {
      driver.declineOffer(offer.getId)
      logger info "declining offer"
    }

  }

  def processOffers(driver: SchedulerDriver, offers: List[Offer]) {
    offers.foreach(offer => {
      val nextTask = findSuitableTask(offer)
      if (nextTask.isEmpty) {
        logger debug "declining offer"
        driver.declineOffer(offer.getId)
      } else if (activeExecutor.nonEmpty) {
        launchTask(driver, offer, nextTask.get)
      } else {
        logger warn "executor info is not set"
        driver.declineOffer(offer.getId)
      }
    })
  }

  def createTask(offer: Offer, executorInfo: ExecutorInfo, id: String, dataToSend: String): TaskInfo = {
    val taskId = TaskID.newBuilder().setValue(id).build()

    TaskInfo.newBuilder()
      .setName("task-" + taskId.getValue)
      .setTaskId(taskId)
      .setData(ByteString.copyFrom("dataPiece*" + dataToSend, "UTF-8"))
      .setSlaveId(offer.getSlaveId)
      .addResources(Resource.newBuilder()
      .setName("cpus")
      .setType(Value.Type.SCALAR)
      .setScalar(Value.Scalar.newBuilder().setValue(1)))
      .addResources(Resource.newBuilder()
      .setName("mem")
      .setType(Value.Type.SCALAR)
      .setScalar(Value.Scalar.newBuilder().setValue(128)))
      .setExecutor(executorInfo)
      .build()
  }

}
