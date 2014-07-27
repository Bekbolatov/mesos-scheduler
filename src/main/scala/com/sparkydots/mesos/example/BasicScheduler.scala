package com.sparkydots.mesos.example

import java.util

import akka.actor.Actor
import com.google.common.collect.Lists
import com.google.protobuf.ByteString
import com.sparkydots.mesos.framework.scheduler._
import org.apache.mesos.Protos._
import org.apache.mesos.SchedulerDriver

import scala.collection.JavaConverters._

class BasicScheduler(executorInfo: ExecutorInfo, actor: SchedulerActor, numberOfTasks: Int = 3) extends LoggingMesosScheduler {

  var num = 0

  override def resourceOffers(driver: SchedulerDriver, offers: util.List[Offer]) {
    super.resourceOffers(driver, offers)

    logger info "MORE"
    offers.asScala.map(offer => {
      logger info offer.toString
    })

    if (num >= numberOfTasks)
      return

    val offer = offers.get(0)

    val taskId = TaskID.newBuilder().setValue("ID32" + num).build()

    val task = TaskInfo.newBuilder()
      .setName("task-" + taskId.getValue)
      .setTaskId(taskId)
      .setData(ByteString.copyFrom("dataPiece*" + num, "UTF-8"))
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

    val filters = Filters.newBuilder().setRefuseSeconds(1).build()
    driver.launchTasks(Lists.newArrayList(offer.getId), Lists.newArrayList(task), filters)
    num = num + 1
  }
}
