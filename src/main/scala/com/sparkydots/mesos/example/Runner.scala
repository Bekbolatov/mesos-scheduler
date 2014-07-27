package com.sparkydots.mesos.example

import akka.actor.{ActorSystem, Props}
import com.sparkydots.mesos.framework.scheduler.akka.{StopDriver, Initialize, NewTask, SchedulerActor}
import org.apache.mesos.Protos._

import scala.io.StdIn


object BasicSchedulerRunner extends App {

  def createExecutorInfo(options: SchedulerOptions,
                         executorId: String = "executorDefault",
                         executorName: String = "MyExecutor",
                         executorSource: String = "originalSpawn"): ExecutorInfo = {
    val commandInfo = CommandInfo.newBuilder()
      .setValue(s"${options.java} -Djava.library.path=${options.lib} -classpath ${options.cp} ${options.executorRunner} ${options.masterAddress}")

    ExecutorInfo.newBuilder()
      .setExecutorId(ExecutorID.newBuilder().setValue(executorId))
      .setCommand(commandInfo)
      .setName(executorName)
      .setSource(executorSource)
      .build()
  }


  val options =  OptionsLocal //OptionsAWS //

  val system = ActorSystem(options.actorSystemName)
  val schedulerActor = system.actorOf(Props[SchedulerActor], options.actorName)

  schedulerActor ! Initialize(createExecutorInfo(options), options.frameworkName, options.masterAddress)

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
            schedulerActor ! NewTask(tokens(1), "id" + numTasks)
        case _ =>
          println("_")
      }
    } else {
      schedulerActor ! StopDriver()
      system.shutdown()
      println("exiting...")
    }
    numTasks = numTasks + 1
  }

}


