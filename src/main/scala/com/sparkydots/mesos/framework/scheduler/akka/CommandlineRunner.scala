package com.sparkydots.mesos.framework.scheduler.akka

import akka.actor.{ActorSystem, Props}
import com.sparkydots.mesos.framework.scheduler.akka.SchedulerMessages._
import org.apache.mesos.Protos._

import scala.io.StdIn


object CommandlineRunner {

  def run(options: SchedulerOptions) {

    val system = ActorSystem(options.actorSystemName)
    val schedulerActor = system.actorOf(Props[Scheduler], options.actorName)

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

    var numTasks = 0
    var ok = true
    while (ok) {
      val ln = StdIn.readLine()
      ok = ln != null

      val code = ln.trim().toLowerCase

      if (ok && !code.isEmpty) {
        val code = ln.trim().toLowerCase
        val tokens = code.split(" ")
        tokens(0) match {

          case "connect" =>
            schedulerActor ! Initialize(createExecutorInfo(options), options.frameworkName, options.masterAddress)
            println("starting a new driver and registering anew ...")
          case "disconnect" =>
            schedulerActor ! StopDriver()
            println("stopping driver and de-registering...")
          case "failover" =>
            schedulerActor ! StopDriver(true)
            println("stopping driver for failover and not de-registering...")
          case "reconnect" =>
            schedulerActor ! Reinitialize()
            println("starting driver after failover and re-registering on same framework id...")

          case "send" =>
            if (tokens.length > 1) {
              schedulerActor ! NewTaskRequest(tokens(1), "id" + numTasks)
              numTasks = numTasks + 1
              println("sending a new task request")
            } else {
              println("need to supply a message:  send <msg>")
            }

          case "exit" =>
            ok = false

          case _ =>
            println("unknown command. Usage: connect|disconnect|failover|reconnect|send <msg>|exit")
        }
      }

    }

    schedulerActor ! StopDriver()
    system.shutdown()
    println("exiting...")

  }
}


