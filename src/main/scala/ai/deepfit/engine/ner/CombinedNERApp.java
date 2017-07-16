package ai.deepfit.engine.ner;

/**
 * Created by alvinjin on 2017-07-12.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class CombinedNERApp {


    //@Test
    //public void basic() {
    public static void main(String[] args){
        String rules = "rules/cv.rules.txt";

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma, ner, techterm, tokensregexdemo, regexner");
        props.setProperty("customAnnotatorClass.tokensregexdemo", "edu.stanford.nlp.pipeline.TokensRegexNERAnnotator");
        props.setProperty("tokensregexdemo.rules", rules);
        props.setProperty("customAnnotatorClass.techterm", "ai.deepfit.engine.annotator.CustomTermAnnotator");
        props.setProperty("custom.techterm.file", "rules/techterm.txt");

        props.put("regexner.mapping", "locations.txt");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


        String[] tests =
                {
                        "Alvin was born on October 2000, 2010 to Present, 2010 to Now.",
                        "Data Platform Tech Lead, Senior Data Scientist, Intern, Software Development Engineer, Contact: http://alvincjin.blogspot.com Email: alvincjin@gmail.com cellphone: 647-636-3476.",
                        "Master of Mathematics in Hadoop, Cassandra, Distributed Systems. School of Computer Science University of Waterloo from 2010 to 2013.",
                        "York University - Schulich School of Business, MBA, Marketing, from April 2010 - June 2011.",
                        "In April 2013, Changjiu Jin was in School of Computer Science, London University in 2010. College of Toronto, University of Waterloo during 2010-2013.",
                        "Partial invoice (€100,000, so roughly 40%) for the consignment C27655 we shipped on 15th August to Waterloo from the Bachelor of Arts depot. INV2345 is for the balance.. Customer contact (Sigourney) says they will pay this on the usual credit terms (30 days)."
                };

        List<EmbeddedToken> tokens = new ArrayList<>();

        for (String s : tests) {

            // run all Annotators on the passed-in text
            Annotation document = new Annotation(s);
            pipeline.annotate(document);
            //PrintWriter xmlOut = new PrintWriter("xmlOutput.xml");
            //pipeline.xmlPrint(document, xmlOut)

            // these are all the sentences in this document
            // a CoreMap is essentially a Map that uses class objects as keys and has values with
            // custom types
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
            StringBuilder sb = new StringBuilder();

            //I don't know why I can't get this code out of the box from StanfordNLP, multi-token entities
            //are far more interesting and useful..
            //TODO make this code simpler..
            for (CoreMap sentence : sentences) {
                // traversing the words in the current sentence, "O" is a sensible default to initialise
                // tokens to since we're not interested in unclassified / unknown things..
                String prevNe = "O";
                String currNe = "O";
                boolean newToken = true;

                for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

                    String word = token.get(TextAnnotation.class);
                    currNe = token.get(NamedEntityTagAnnotation.class);
                    //String tag = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                    System.out.println(word + " : " + currNe + " -> " + prevNe);
                    // Strip out "O"s completely, makes code below easier to understand
                   if (currNe.equals("O")) {

                        if (!prevNe.equals("O") && (sb.length() > 0)) {
                            handleEntity(prevNe, sb, tokens); //process the new entity group
                            newToken = true;
                        }
                        continue;
                    }

                    if (newToken) {
                        prevNe = currNe;
                        newToken = false;
                        sb.append(word);
                        continue;
                    }

                    //if the contentated tokens are in the same category.
                    if (currNe.equals(prevNe)) {
                        sb.append(" " + word);// add current token to the previous one, then check the next token
                    } else {
                        // We're done with the current entity - print it out and reset
                        handleEntity(prevNe, sb, tokens);
                        newToken = true;
                    }
                    prevNe = currNe; //move to the next token

                }
            }

        }
        System.out.println("######################################");
        for (EmbeddedToken token : tokens) {
            System.out.println(token.getName()+" "+token.getValue());
        }

    }

    private static void handleEntity(String label, StringBuilder sb, List tokens) {
        tokens.add(new EmbeddedToken(sb.toString(), label));
        sb.setLength(0);
    }

}

class EmbeddedToken {

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public EmbeddedToken(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }
}