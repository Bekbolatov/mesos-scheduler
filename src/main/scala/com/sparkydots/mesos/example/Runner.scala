package com.sparkydots.mesos.example

import akka.actor.{Props, ActorSystem}
import com.sparkydots.mesos.framework.executor.ExecutorActor
import com.sparkydots.mesos.framework.scheduler.{NewTask, NewExecutorInfo, SchedulerActor}
import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos._

import scala.io.StdIn


object BasicSchedulerRunner extends App {

  val eo = OptionsLocalScalaExecutor

  val system = ActorSystem("SchedulerSystem")
  val schedulerActor = system.actorOf(Props[SchedulerActor], "schedulerActor")

  val commandInfo = CommandInfo.newBuilder()
    .setValue(s"${eo.java} -Djava.library.path=${eo.lib} -classpath ${eo.cp} ${eo.executorRunner} ${eo.masterAddress}")
  val executorInfo =
    ExecutorInfo.newBuilder()
      .setExecutorId(ExecutorID.newBuilder().setValue("executorJacob"))
      .setCommand(commandInfo)
      .setName("MyEXECUTOR")
      .setSource("originalSpawn")
      .build()
  schedulerActor ! NewExecutorInfo(executorInfo)

  val scheduler = new com.sparkydots.mesos.framework.scheduler.ActorMesosScheduler(schedulerActor)


  val frameworkInfo: FrameworkInfo = FrameworkInfo.newBuilder().setUser("").setName("ScalaActorFramework").build()
  val schedulerDriver: MesosSchedulerDriver = new MesosSchedulerDriver(scheduler, frameworkInfo, eo.masterAddress)

  val schedulerDriverMainThread = new Thread() {
    override def run() {
      schedulerDriver.run()
    }
  }.start()

  var numTasks = 0
  var ok = true
  while (ok) {
    val ln = StdIn.readLine()
    ok = ln != null && !ln.trim().equals("exit")

    if (ok) {
      val code = ln.trim().toLowerCase
      println(code)
      val tokens = code.split(" ")
      tokens(0) match {
        case "re" =>
          println(tokens)
          if (tokens.length > 1)
            schedulerActor !  NewTask(tokens(1), "id" + numTasks)
        case _ =>
          println("_")
      }
    } else {
      println("exiting...")
    }
    numTasks = numTasks + 1
  }

  val status = if (schedulerDriver.stop() == Status.DRIVER_STOPPED) 0 else 1

  sys.exit(status)
}


