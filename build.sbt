import sbt.Keys._
import AssemblyKeys._ // put this at the top of the file

name := "mesos-scheduler"

version := "1.0"

scalaVersion := "2.11.1"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases"

libraryDependencies ++= Seq(
  "org.apache.mesos" % "mesos" % "0.19.0",
  "com.google.guava" % "guava" % "17.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.0.0",
  "org.slf4j" % "slf4j-api" % "1.7.7",
  "org.slf4j" % "slf4j-log4j12" % "1.7.7",
  "com.typesafe.akka" %% "akka-actor" % "2.3.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.3",
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "junit" % "junit" % "4.11" % "test",
  "com.novocode" % "junit-interface" % "0.10" % "test",
  "org.scala-sbt" % "launcher-interface" % "0.13.5" % "provided"
)

resolvers += sbtResolver.value

assemblySettings


