package ai.deepfit.engine.ResumeParser;

/**
 * Created by alvinjin on 2017-07-01.
 */

import gate.*;
import gate.Document;
import gate.util.GateException;
import gate.util.Out;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;

import static gate.Utils.stringFor;

public class ParserApp {

    private static final Map<String, String> profileMap;

    static {
        profileMap = new HashMap<String, String>();
        profileMap.put("NameFinder", "name");
        profileMap.put("TitleFinder", "title");
        profileMap.put("EmailFinder", "email");
        profileMap.put("PhoneFinder", "phone");
    }

    public static List<JSONObject> loadGateAndAnnie(List<File> files) throws GateException, IOException {

        Out.prln("Initialising Gate & ANNIE system...");
        Gate.setGateHome(new File("GATEFiles"));
        Gate.init();

        // initialise ANNIE (this may take several minutes)
        Annie annie = new Annie();
        annie.initAnnie();

        // create a GATE corpus and add a document
        Corpus corpus = Factory.newCorpus("Annie corpus");

        List<Document> resumes = new ArrayList<>();
        for (File file : files) {
            FeatureMap params = Factory.newFeatureMap();
            params.put("sourceUrl", file.toURI().toURL());
            params.put("preserveOriginalContent", new Boolean(true));
            params.put("collectRepositioningInfo", new Boolean(true));

            Document resume = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);
            resumes.add(resume);
        }

        corpus.addAll(resumes);

        // tell the pipeline about the corpus and run it
        annie.setCorpus(corpus);
        annie.execute();

        Iterator iter = corpus.iterator();
        List<JSONObject> results = new ArrayList<>();//JSONObject();
        Out.prln("Started parsing...");

        while (iter.hasNext()) { // should technically be while but I am just dealing with one document

            JSONObject cvJson = new JSONObject();
            Document doc = (Document) iter.next();

            JSONObject profileJSON = getProfile(doc);
            cvJson.put("basics", profileJSON);

            String[] otherSections = new String[]{"education_and_training", "skills", "accomplishments", "work_experience", "misc"};
            for (String otherSection : otherSections) {

                JSONArray sections = getSection(doc, otherSection);
                cvJson.put(otherSection, sections);

            }

            results.add(cvJson);

        }
        return results;
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


    private static JSONObject getProfile(Document doc) {

        System.out.println(doc.getName());
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

        String inputDir = "data/cv/stage/";
        File dir = new File(inputDir);
        List<File> fileList = Arrays.asList(dir.listFiles());
        String outputDir = "data/cv/output/";

        try {

            List<JSONObject> parsedJSON = loadGateAndAnnie(fileList);

            parsedJSON.forEach(json -> {

                String outputFileName = outputDir + UUID.randomUUID() + ".json";
                try {
                    FileWriter jsonFileWriter = new FileWriter(outputFileName);
                    jsonFileWriter.write(json.toJSONString());
                    jsonFileWriter.flush();
                    jsonFileWriter.close();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    System.out.println("Sad Face :( .Something went wrong.");
                    e.printStackTrace();
                }
            });


        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Sad Face :( .Something went wrong.");
            e.printStackTrace();
        }
    }
}
