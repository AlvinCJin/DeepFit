
import sbt._
import sbt.Keys._
import sbtassembly.AssemblyKeys._

object Build extends sbt.Build {

  lazy val hlService = Project("deepfit-engine", file("."))
    .settings(commonSettings: _*)
    .settings(libraryDependencies ++= Dependencies.allDeps)
    .settings(assemblyJarName in assembly := "deepfit-engine.jar")

  lazy val commonSettings = Seq(
    organization := "ai.deepfit",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := "2.11.8",

    test in assembly := {},

    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-Ywarn-dead-code",
      "-Ywarn-infer-any",
      "-Ywarn-unused-import",
      "-Xfatal-warnings",
      "-Xlint"
    )
  )

  resolvers ++= Seq(
    "confluent" at "http://packages.confluent.io/maven/",
    "Typesafe Simple Repository" at "http://repo.typesafe.com/typesafe/simple/maven-releases/",
    "Databricks" at "https://dl.bintray.com/spark-packages/maven/"
  )

}
