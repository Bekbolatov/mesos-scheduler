package com.sparkydots.mesos.framework.scheduler.akka

/**
 * Base scheduler options, need to be overwritten
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/27/14 3:11 PM
 */
class SchedulerOptions {
  val frameworkName = "frameworkName"
  val actorSystemName = "SchedulerActorSystem"
  val actorName = "SchedulerActor"

  val masterAddress = "127.0.0.1:5050"
  val java = "/usr/bin/java"
  val lib = "/usr/local/Cellar/mesos/0.19.0/lib"
  val other = ""
  val cp = "/Users/renatb/projects/90_scratch/NamesOnMesos/build/libs/NamesOnMesos-1.0.jar"
  val executorRunner = "com.sparkydots.mesos.example.BasicExecutorRunner"


  def cmdLine(options: SchedulerOptions) = s"${options.java} -Djava.library.path=${options.lib} -classpath ${options.cp} ${options.other} ${options.executorRunner} ${options.masterAddress}"
}