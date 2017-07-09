package ai.deepfit.engine.ResumeParser;

/**
 * Created by alvinjin on 2017-07-01.
 */

import gate.*;
import gate.util.GateException;
import gate.util.Out;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static gate.Utils.stringFor;

public class ParserApp {

    private static final Map<String, String> profileMap;

    static {
        profileMap = new HashMap<String, String>();
        profileMap.put("NameFinder", "name");
        profileMap.put("TitleFinder", "title");
        profileMap.put("EmailFinder", "email");
        profileMap.put("PhoneFinder", "phone");
        //profileMap.put("AddressFinder", "address");
        //profileMap.put("URLFinder", "url");
    }

    public static JSONObject loadGateAndAnnie(File file) throws GateException, IOException {

        Out.prln("Initialising Gate & ANNIE system...");
        Gate.setGateHome(new File("GATEFiles"));
        Gate.init();

        // initialise ANNIE (this may take several minutes)
        Annie annie = new Annie();
        annie.initAnnie();

        // create a GATE corpus and add a document
        Corpus corpus = Factory.newCorpus("Annie corpus");

        FeatureMap params = Factory.newFeatureMap();
        params.put("sourceUrl", file.toURI().toURL());
        params.put("preserveOriginalContent", new Boolean(true));
        params.put("collectRepositioningInfo", new Boolean(true));

        Document resume = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);
        corpus.add(resume);

        // tell the pipeline about the corpus and run it
        annie.setCorpus(corpus);
        annie.execute();

        Iterator iter = corpus.iterator();
        JSONObject parsedJSON = new JSONObject();
        Out.prln("Started parsing...");

        if (iter.hasNext()) { // should technically be while but I am just dealing with one document

            Document doc = (Document) iter.next();

            JSONObject profileJSON = getProfile(doc);
            parsedJSON.put("basics", profileJSON);

            String[] otherSections = new String[]{"summary", "education_and_training", "skills", "accomplishments", "awards", "misc"};
            for (String otherSection : otherSections) {

                JSONArray sections = getSection(doc, otherSection);
                parsedJSON.put(otherSection, sections);

            }

            JSONArray workExpJson = getWorkExp(doc);
            parsedJSON.put("work_experience", workExpJson);

        }
        return parsedJSON;
    }

    private static JSONArray getSection(Document doc, String header) {

        AnnotationSet curAnnSet = doc.getAnnotations().get(header);
        JSONArray sections = new JSONArray();

        curAnnSet.forEach(currAnnot -> {
            JSONObject section = new JSONObject();
            String key = (String) currAnnot.getFeatures().get("sectionHeading");
            String value = stringFor(doc, currAnnot);
            section.put(key, value);

            sections.add(section);
        });

        return sections;
    }

    private static JSONArray getWorkExp(Document doc) {

        AnnotationSet curAnnSet = doc.getAnnotations().get("work_experience");
        JSONArray workExpsJson = new JSONArray();

        curAnnSet.forEach(currAnnot -> {

            JSONObject workExperience = new JSONObject();
            String key = (String) currAnnot.getFeatures().get("sectionHeading");
            if (key.equals("work_experience_marker")) {

                String[] annotations = new String[]{"date_start", "date_end", "jobtitle", "organization"};
                for (String annotation : annotations) {
                    String v = (String) currAnnot.getFeatures().get(annotation);
                    if (!StringUtils.isBlank(v)) {
                        workExperience.put(annotation, v);
                    }
                }

                key = "descriptions";

            }
            String value = stringFor(doc, currAnnot);
            if (!StringUtils.isBlank(key) && !StringUtils.isBlank(value)) {
                workExperience.put(key, value);
            }
            if (!workExperience.isEmpty()) {
                workExpsJson.add(workExperience);
            }

        });

        return workExpsJson;

    }


    private static JSONObject getProfile(Document doc) {

        JSONObject profileJSON = new JSONObject();
        AnnotationSet defaultAnnotSet = doc.getAnnotations();

        profileMap.forEach((annSection, name) -> {

            AnnotationSet curAnnSet = defaultAnnotSet.get(annSection);
            JSONArray sectionArray = new JSONArray();

            curAnnSet.forEach(currAnnot -> {
                String s = stringFor(doc, currAnnot);
                sectionArray.add(s);
            });

            profileJSON.put(name, sectionArray);
        });

        return profileJSON;

    }


    public static void main(String[] args) {

        String inputFileName = "data/cv/stage/Alvin_Indeed.html";
        File f = new File(inputFileName);

        String outputFileName = "data/output/" + FilenameUtils.removeExtension(f.getName()) + ".json";
        try {
            File tikkaConvertedFile = new File(inputFileName);
            if (tikkaConvertedFile != null) {
                JSONObject parsedJSON = loadGateAndAnnie(tikkaConvertedFile);

                Out.prln("Writing to output...");
                FileWriter jsonFileWriter = new FileWriter(outputFileName);
                jsonFileWriter.write(parsedJSON.toJSONString());
                jsonFileWriter.flush();
                jsonFileWriter.close();
                Out.prln("Output written to file " + outputFileName);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Sad Face :( .Something went wrong.");
            e.printStackTrace();
        }
    }
}
