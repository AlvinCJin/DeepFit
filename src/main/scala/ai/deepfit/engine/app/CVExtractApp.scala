package ai.deepfit.engine.app

import ai.deepfit.engine.config.Config
import ai.deepfit.engine.parser.TextExtractor
import java.io._

import org.apache.spark.sql.SparkSession

/**
  * Created by alvinjin on 2017-06-29.
  */
object CVExtractApp extends App with Config {

  val PREFIX = 5

  val spark = SparkSession
    .builder()
    .master(sparkMaster)
    .appName("YelpRecommanderApp")
    .getOrCreate()

  import spark.implicits._

  val textExtractor = new TextExtractor()


  val files = spark.sparkContext.binaryFiles(cvInputPath)
    .map{ binFile =>
      val file = new File(binFile._1.drop(PREFIX))
      val content = textExtractor
        .extractText(file)
        .replaceAll("(?m)^[ \t]*\r?\n", "")

      (file.getName, content)
    }.toDS//("filename","content")

  val assembleMatrix = new AssembleDocumentTermMatrix(spark)

  val stopWordsFile = "data/dict/stopwords.txt"
  val terms = assembleMatrix.contentsToTerms(files, stopWordsFile)

  terms.foreach(f => println(f._2.mkString(",")))//.show(2)


}
