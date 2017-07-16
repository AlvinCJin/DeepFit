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

  val tikaParser =  "org.apache.tika" % "tika-parsers" % "1.16"
  val tikaCore =  "org.apache.tika" % "tika-core" % "1.16"

  val config = "com.typesafe" % "config" % "1.3.1"

  val corenlp = "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0"
  val corenlpModel = "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0" classifier "models"

  val gate = "uk.ac.gate" % "gate-core" % "8.4.1"
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


  val nlp = Seq(corenlp, corenlpModel, gate)

  val utils = Seq(tikaParser, tikaCore, config)

  val mainDeps = spark ++ nlp ++ utils

  val testDeps = test

  val allDeps = mainDeps ++ testDeps
}
