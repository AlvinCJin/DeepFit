import sbt._


object Dependencies {



  lazy val versions = Map(
    "kafka" -> "0.10.2.0",
    "confluent" -> "3.2.0",
    "spark" -> "2.1.1"
  )
  //Spark
  val sparkStream = "org.apache.spark" %% "spark-streaming" % versions("spark")
  val sparkCore = "org.apache.spark" %% "spark-core" % versions("spark")
  val sparkSql = "org.apache.spark" %% "spark-sql" % versions("spark")
  val sparkML = "org.apache.spark" %% "spark-mllib" % versions("spark")
  val sparkKafka = "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % versions("spark")


  val config = "com.typesafe" % "config" % "1.3.1"
  //Test
  val specsCore = "org.specs2" %% "specs2-core" % "3.6.4"
  val specsJunit = "org.specs2" %% "specs2-junit" % "3.6.4"
  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.6"



  val resolvers = Seq(
      Resolver sonatypeRepo "public",
      Resolver typesafeRepo "releases"
    )


  val spark = Seq(sparkCore, sparkStream, sparkSql, sparkML, sparkKafka)

  val test = Seq(specsCore, specsJunit, scalaTest)

  val utils = Seq(config)

  val mainDeps = spark ++ utils

  val testDeps = test

  val allDeps = mainDeps ++ testDeps
}
