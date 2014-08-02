package com.sparkydots.mesos.example

import com.sparkydots.mesos.example.LocalSchedulerOptions._
import com.sparkydots.mesos.framework.scheduler.akka.CommandlineRunner

/**
 * Example running the scheduler
 * @author Renat Bekbolatov (renatb@sparkydots.com) 7/27/14 2:42 PM
 */
object Runner extends App {
  CommandlineRunner.run(OptionsLocalScalaLauncherExecutor)
}
