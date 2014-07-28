package com.sparkydots.mesos.framework.scheduler.akka

import akka.actor.Actor
import com.google.common.collect.Lists
import com.google.protobuf.ByteString
import com.sparkydots.mesos.framework.scheduler.akka.SchedulerMessages._
import com.typesafe.scalalogging.LazyLogging
import org.apache.mesos.Protos._
import org.apache.mesos.{SchedulerDriver, Scheduler => MesosScheduler}

import scala.collection.mutable

/**
 * Wraps MesosScheduler and the driver through Akka Actor, allowing for more succinct task management
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/26/14 9:09 PM
 */

class Scheduler extends Actor with LazyLogging {

  def receive: Receive = {
    case Initialize(executorInfo, frameworkName, masterAddress) =>
      initialize(executorInfo, frameworkName, masterAddress)
    case Reinitialize(masterAddress) =>
      reinitialize(masterAddress)
    case StopDriver(failover) =>
      stopDriver(failover)
    case SetMaxTries(newMaxTries) =>
      _maxTries = newMaxTries
    case NewTaskRequest(taskDescription, id) =>
      addNewTask(taskDescription, id)

    case Registered(frameworkId: FrameworkID, masterInfo: MasterInfo) =>
      processRegistered(frameworkId, masterInfo)
    case Reregistered(masterInfo: MasterInfo) =>
      processReregistered(masterInfo)
    case ResourceOffers(driver, offers) =>
      processResourceOffers(driver, offers)
    case StatusUpdate(driver, status) =>
      processStatusUpdate(driver, status)

    case _ =>
      logger warn "no message matched"
  }

  private case class ActiveTask(taskInfo: TaskInfo, tries: Int, originalDescription: String)

  private case class NewTask(taskDescription: String, baseTaskId: String)

  private val taskQueue = new mutable.Queue[NewTask]
  private val activeTasks = new mutable.HashMap[String, ActiveTask]

  private var _maxTries = 3
  private var _activeExecutor: Option[ExecutorInfo] = None
  private var _scheduler: Option[MesosScheduler] = None
  private var _frameworkInfo: Option[FrameworkInfo] = None
  private val _driverManager = new DriverManager
  private var _frameworkId: Option[FrameworkID] = None
  private var _masterInfo: Option[MasterInfo] = None


  private def initialize(executorInfo: ExecutorInfo, frameworkName: String, masterAddress: String) {
    taskQueue.clear()
    activeTasks.clear()
    _activeExecutor = Some(executorInfo)
    _scheduler = Some(new PassthruMesosScheduler(self))
    _frameworkInfo = Some(FrameworkInfo.newBuilder().setUser("").setName(frameworkName).build())
    _driverManager.init(_scheduler.get, _frameworkInfo.get, masterAddress)
  }

  private def reinitialize(masterAddress: Option[String]) {
    if (_frameworkId.nonEmpty) {
      _driverManager.reinit(_scheduler.get, _frameworkId.get, masterAddress)
    }
  }

  private def stopDriver(failover: Boolean) {
    _driverManager.stop(failover)
  }

  private def addNewTask(taskDescription: String, baseTaskId: String) {
    taskQueue += NewTask(taskDescription, baseTaskId)
  }

  //

  private def processStatusUpdate(driver: SchedulerDriver, status: TaskStatus) {
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
      if (activeTaskLine.get.tries < _maxTries) {
        addNewTask(activeTaskLine.get.originalDescription, commonTaskId)
      } else {
        activeTasks.remove(commonTaskId)
        logger error("task {} could not complete after {} retries", commonTaskId, _maxTries.toString)
      }
    } else if (state == TaskState.TASK_FINISHED) {
      activeTasks.remove(commonTaskId)
    } else {
      logger warn("unhandled task state message {}", state.toString)
    }
  }

  private def badState(state: TaskState): Boolean = {
    state == TaskState.TASK_FAILED ||
      state == TaskState.TASK_KILLED ||
      state == TaskState.TASK_LOST
  }


  private def findSuitableTask(offer: Offer): Option[NewTask] = {
    if (taskQueue.isEmpty) {
      None
    } else {
      Some(taskQueue.dequeue())
    }
  }

  private def makeNewTaskId(common: String): Option[Tuple2[String, Int]] = {
    val activeTask = activeTasks.get(common)
    if (activeTask.nonEmpty) {
      val count = activeTask.get.tries
      if (count > _maxTries) {
        None
      } else {
        Some(Tuple2(common + "-" + (count + 1), count))
      }
    } else {
      Some(Tuple2(common + "-1", 0))
    }
  }

  private def launchTask(driver: SchedulerDriver, offer: Offer, taskData: NewTask) {
    val newId = makeNewTaskId(taskData.baseTaskId)
    if (newId.nonEmpty) {
      val taskInfo = createTask(offer, _activeExecutor.get, newId.get._1, taskData.taskDescription)
      val filters = Filters.newBuilder().setRefuseSeconds(1).build()
      driver.launchTasks(Lists.newArrayList(offer.getId), Lists.newArrayList(taskInfo), filters)

      activeTasks += (taskInfo.getTaskId.getValue -> ActiveTask(taskInfo, newId.get._2 + 1, taskData.taskDescription))
      logger info("launching task {}", newId)
    } else {
      driver.declineOffer(offer.getId)
      logger info "declining offer"
    }

  }


  //
  private def processRegistered(id: FrameworkID, info: MasterInfo) {
    _frameworkId = Some(id)
    _masterInfo = Some(info)
  }

  private def processReregistered(info: MasterInfo) {
    _masterInfo = Some(info)
  }

  private def processResourceOffers(driver: SchedulerDriver, offers: List[Offer]) {
    offers.foreach(offer => {
      val nextTask = findSuitableTask(offer)
      if (nextTask.isEmpty) {
        logger debug "declining offer"
        driver.declineOffer(offer.getId)
      } else if (_activeExecutor.nonEmpty) {
        launchTask(driver, offer, nextTask.get)
      } else {
        logger warn "executor info is not set"
        driver.declineOffer(offer.getId)
      }
    })
  }

  private def createTask(offer: Offer, executorInfo: ExecutorInfo, id: String, dataToSend: String): TaskInfo = {
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
