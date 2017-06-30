package ai.deepfit.engine.config

import com.typesafe.config.ConfigFactory
/**
  * Created by alvinjin on 2017-06-29.
  */

trait Config {

  val appConfig = ConfigFactory.load()

  val sparkConfig = appConfig.getConfig("spark")
  val sparkMaster = sparkConfig.getString("masterUrl")
  val checkpointDir = sparkConfig.getString("checkpointDir")

  val cvInputPath = appConfig.getString("cvInputPath")
  val cvOutputPath = appConfig.getString("cvOutputPath")
  val jdInputPath = appConfig.getString("jdInputPath")

}

