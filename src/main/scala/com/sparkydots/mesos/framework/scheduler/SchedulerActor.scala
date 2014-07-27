package com.sparkydots.mesos.framework.scheduler

import akka.actor.Actor
import com.google.common.collect.Lists
import com.google.protobuf.ByteString
import com.typesafe.scalalogging.LazyLogging
import org.apache.mesos.Protos._
import org.apache.mesos.SchedulerDriver

import scala.collection.mutable

/**
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/26/14 9:09 PM
 */

case class TaskDataEntry(taskInfo: TaskInfo, tries: Int, originalDescription: String)

class SchedulerActor extends Actor with LazyLogging {

  val MAX_TRIES = 3
  val taskQueue = new mutable.Queue[Tuple2[String, String]]
  var currentExecutorInfo: Option[ExecutorInfo] = None

  val activeTasks = new mutable.HashMap[String, TaskDataEntry]

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
      if (activeTaskLine.get.tries < MAX_TRIES) {
        addNewTask(activeTaskLine.get.originalDescription, commonTaskId)
      } else {
        activeTasks.remove(commonTaskId)
        logger error("task {} could not complete after {} retries", commonTaskId, MAX_TRIES.toString)
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

  def receive: Receive = {
    case NewExecutorInfo(executorInfo) =>
      currentExecutorInfo = Some(executorInfo)
    case NewTask(taskDescription, id) =>
      addNewTask(taskDescription, id)
    case ResourceOffers(driver, offers) =>
      processOffers(driver, offers)
    case StatusUpdate(driver, status) =>
      processStatusUpdate(driver, status)
    case _ =>
      logger warn "no message matched"

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
      if (count > MAX_TRIES) {
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
      val taskInfo = createTask(offer, currentExecutorInfo.get, newId.get._1, taskData._1)
      val filters = Filters.newBuilder().setRefuseSeconds(1).build()
      driver.launchTasks(Lists.newArrayList(offer.getId), Lists.newArrayList(taskInfo), filters)

      activeTasks += (taskInfo.getTaskId.getValue -> TaskDataEntry(taskInfo, newId.get._2 + 1, taskData._1))
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
      } else if (currentExecutorInfo.nonEmpty) {
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
