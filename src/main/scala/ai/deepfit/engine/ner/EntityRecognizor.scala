package ai.deepfit.engine.ner

/**
  * Created by alvinjin on 2017-07-12.
  */

import java.io.{PrintWriter}
import java.util._

import edu.stanford.nlp.ie.AbstractSequenceClassifier
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}

import scala.collection.JavaConverters._

object EntityRecognizor extends App {

  val xmlOut: PrintWriter = new PrintWriter("xmlOutput.xml")
  val props: Properties = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
  props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner")
  props.put("regexner.mapping", "locations.txt")
  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  val doc = "Alvin Jin obtains Bachelor of Science in 2010. He was studying in University of Waterloo during June 2010-June 2013."

  val annotation: Annotation = new Annotation(doc)

  pipeline.annotate(annotation)
  pipeline.xmlPrint(annotation, xmlOut)

  /**
    * An Annotation is a Map and you can get and use the various analyses individually.
    */

  val sentences = annotation.get(classOf[SentencesAnnotation])

  for (sentence <- sentences.asScala;
       token <- sentence.get(classOf[TokensAnnotation]).asScala) {
    // this is the text of the token
    val word = token.get(classOf[TextAnnotation])
    // this is the POS tag of the token
    val pos = token.get(classOf[PartOfSpeechAnnotation])
    // this is the NER label of the token
    val ne = token.get(classOf[NamedEntityTagAnnotation])

    println(word + " " + pos + " " + ne )

  }


  combineNERSequence(doc)

 def combineNERSequence(text: String):Unit = {

    val serializedClassifier: String = "edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz"
    val classifier: AbstractSequenceClassifier[CoreLabel]  =  CRFClassifier
        .getClassifier(serializedClassifier);

    println(classifier.classifyWithInlineXML(text))
   //println(classifier.classifyToString(text, "xml", true))
  }


}
