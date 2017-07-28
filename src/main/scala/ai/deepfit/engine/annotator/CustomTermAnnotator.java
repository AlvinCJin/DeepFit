package ai.deepfit.engine.annotator;

/**
 * Created by alvinjin on 2017-07-15.
 */

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.util.ArraySet;

import java.util.*;

public class CustomTermAnnotator implements Annotator {

    HashMap<String,String> wordToTag = new HashMap<String,String>();

    public CustomTermAnnotator(String name, Properties props) {
        // load the lemma file
        // format should be tsv with word and lemma
        String termFile = props.getProperty("custom.techterm.file");
        List<String> termEntries = IOUtils.linesFromFile(termFile);
        for (String termEntry : termEntries) {
            wordToTag.put(termEntry.split("\\t")[0], termEntry.split("\\t")[1]);
        }
    }

    public void annotate(Annotation annotation) {
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            String term = wordToTag.getOrDefault(token.word().toLowerCase(), token.ner());
            token.set(CoreAnnotations.NamedEntityTagAnnotation.class, term);
        }
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.TextAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class,
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.NamedEntityTagAnnotation.class
        )));
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(CoreAnnotations.NamedEntityTagAnnotation.class);
    }

}
