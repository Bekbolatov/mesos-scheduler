package com.sparkydots.mesos

import org.apache.mesos.ExecutorDriver
import org.apache.mesos.Protos.TaskInfo

/**
 * Created by renatb on 7/26/14.
 */
package object example {

  object OptionsLocal {
    val masterAddress = "127.0.0.1:5050"
    val java = "/usr/bin/java"
    val lib = "/usr/local/Cellar/mesos/0.19.0/lib"
    val cp = "/Users/renatb/projects/90_scratch/NamesOnMesos/build/libs/NamesOnMesos-1.0.jar"
    val executorRunner = "com.sparkydots.mesos.example.BasicExecutorRunner"
  }

  object OptionsLocalScalaExecutor {
    val masterAddress = "127.0.0.1:5050"
    val java = "/usr/bin/java"
    val lib = "/usr/local/Cellar/mesos/0.19.0/lib"
    val cp = "/Users/renatb/projects/90_scratch/Bekbolatov/mesos-scheduler/target/scala-2.11/mesos-scheduler-assembly-1.0.jar"
    val executorRunner = "com.sparkydots.mesos.example.BasicExecutor"
  }

  object OptionsAWS {
    val masterAddress = "awsserver.vicinitalk.com:5050"
    //zk://awsserver.vicinitalk.com:2181/mesos2
    val java = "/usr/bin/java"
    val lib = "/usr/local/lib"
    val cp = "/home/ubuntu/other/NamesOnMesos/build/libs/NamesOnMesos-1.0.jar"
    val executorRunner = "com.sparkydots.mesos.example.BasicExecutorRunner"
  }

  abstract class Message

  case class SimpleMessage(message: String) extends Message

  case class TaskInfoMessage(driver: ExecutorDriver, taskInfo: TaskInfo) extends Message

}
