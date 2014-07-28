package com.sparkydots.mesos.example

import com.sparkydots.mesos.framework.scheduler.akka.SchedulerOptions

/**
 * Some scheduler startup options
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/27/14 2:44 PM
 */
object SchedulerOptions {

  object OptionsLocal extends SchedulerOptions() {
    override val masterAddress = "127.0.0.1:5050"
    override val java = "/usr/bin/java"
    override val lib = "/usr/local/Cellar/mesos/0.19.0/lib"
    override val cp = "/Users/renatb/projects/90_scratch/NamesOnMesos/build/libs/NamesOnMesos-1.0.jar"
    override val executorRunner = "com.sparkydots.mesos.example.BasicExecutorRunner"
  }

  object OptionsLocalScalaExecutor extends SchedulerOptions() {
    override val masterAddress = "127.0.0.1:5050"
    override val java = "/usr/bin/java"
    override val lib = "/usr/local/Cellar/mesos/0.19.0/lib"
    override val cp = "/Users/renatb/projects/90_scratch/Bekbolatov/mesos-scheduler/target/scala-2.11/mesos-scheduler-assembly-1.0.jar"
    override val executorRunner = "com.sparkydots.mesos.example.BasicExecutor"
  }

  object OptionsAWS extends SchedulerOptions() {
    override val masterAddress = "awsserver.vicinitalk.com:5050"
    //zk://awsserver.vicinitalk.com:2181/mesos2
    override val java = "/usr/bin/java"
    override val lib = "/usr/local/lib"
    override val cp = "/home/ubuntu/other/NamesOnMesos/build/libs/NamesOnMesos-1.0.jar"
    override val executorRunner = "com.sparkydots.mesos.example.BasicExecutorRunner"
    override val frameworkName = "akkaFrameW"
  }

}
