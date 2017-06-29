package ai.deepfit.engine.config

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
/**
  * Created by alvinjin on 2017-06-29.
  */

trait Config {

  val appConfig = ConfigFactory.load()

  val sparkConfig = appConfig.getConfig("spark")
  val sparkMaster = sparkConfig.getString("masterUrl")
  val checkpointDir = sparkConfig.getString("checkpointDir")

  val stackInputPath = appConfig.getString("stackInputPath")
  val outputPath = appConfig.getString("outputPath")
  val yelpInputPath = appConfig.getString("yelpInputPath")

}

