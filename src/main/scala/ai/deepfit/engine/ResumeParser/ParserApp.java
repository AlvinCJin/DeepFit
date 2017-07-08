package ai.deepfit.engine.ResumeParser;

/**
 * Created by alvinjin on 2017-07-01.
 */

import gate.*;
import gate.util.GateException;
import gate.util.Out;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static gate.Utils.stringFor;

public class ParserApp {
    private static File parseToHTMLUsingApacheTikka(String file)
            throws IOException, SAXException, TikaException {
        // determine extension
        String ext = FilenameUtils.getExtension(file);
        String outputFileFormat = "";
        // ContentHandler handler;
        if (ext.equalsIgnoreCase("html") | ext.equalsIgnoreCase("pdf")
                | ext.equalsIgnoreCase("doc") | ext.equalsIgnoreCase("docx")) {
            outputFileFormat = ".html";
            // handler = new ToXMLContentHandler();
        } else if (ext.equalsIgnoreCase("txt") | ext.equalsIgnoreCase("rtf")) {
            outputFileFormat = ".txt";
        } else {
            System.out.println("Input format of the file " + file
                    + " is not supported.");
            return null;
        }
        String OUTPUT_FILE_NAME = FilenameUtils.removeExtension(file)
                + outputFileFormat;
        ContentHandler handler = new ToXMLContentHandler();
        // ContentHandler handler = new BodyContentHandler();
        // ContentHandler handler = new BodyContentHandler(
        // new ToXMLContentHandler());
        InputStream stream = new FileInputStream(file);
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        try {
            parser.parse(stream, handler, metadata);
            FileWriter htmlFileWriter = new FileWriter(OUTPUT_FILE_NAME);
            htmlFileWriter.write(handler.toString());
            htmlFileWriter.flush();
            htmlFileWriter.close();
            return new File(OUTPUT_FILE_NAME);
        } finally {
            stream.close();
        }
    }

    public static JSONObject loadGateAndAnnie(File file) throws GateException, IOException {

        Out.prln("Initialising Gate & ANNIE system...");
        //Gate.setGateHome(new File("path"));
        //Gate.setPluginsHome(new File("path"));
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

        /**
         * #############################################
         */

        Iterator iter = corpus.iterator();
        JSONObject parsedJSON = new JSONObject();
        Out.prln("Started parsing...");
        // while (iter.hasNext()) {
        if (iter.hasNext()) { // should technically be while but I am just
            // dealing with one document

            Document doc = (Document) iter.next();

            JSONObject profileJSON = getProfile(doc);
            parsedJSON.put("basics", profileJSON);

            // awards,credibility,education_and_training,extracurricular,misc,skills,summary
            String[] otherSections = new String[] { "summary", "education_and_training", "skills", "accomplishments", "awards", "misc" };
            for (String otherSection : otherSections) {

                JSONArray sections = getSection(doc, otherSection);
                parsedJSON.put(otherSection, sections);

            }

            JSONArray workExpJson = getWorkExp(doc);
            parsedJSON.put("work_experience", workExpJson);

        }
        return parsedJSON;
    }

    private static JSONArray getSection(Document doc, String header){
        AnnotationSet curAnnSet = doc.getAnnotations().get(header);
        Iterator it = curAnnSet.iterator();
        JSONArray sections = new JSONArray();
        while (it.hasNext()) {
            JSONObject section = new JSONObject();
            Annotation currAnnot = (Annotation) it.next();
            String key = (String) currAnnot.getFeatures().get("sectionHeading");
            String value = stringFor(doc, currAnnot);
            section.put(key, value);

            sections.add(section);
        }
        return sections;
    }

    private static JSONArray getWorkExp(Document doc){

        AnnotationSet curAnnSet = doc.getAnnotations().get("work_experience");
        Iterator it = curAnnSet.iterator();
        JSONArray workExpsJson = new JSONArray();
        while (it.hasNext()) {
            JSONObject workExperience = new JSONObject();
            Annotation currAnnot = (Annotation) it.next();
            String key = (String) currAnnot.getFeatures().get("sectionHeading");
            if (key.equals("work_experience_marker")) {

                String[] annotations = new String[] { "date_start", "date_end", "jobtitle", "organization" };
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

        }
        return workExpsJson;

    }

    private static JSONObject getProfile(Document doc){

        JSONObject profileJSON = new JSONObject();
        // Name
        AnnotationSet defaultAnnotSet = doc.getAnnotations();
        AnnotationSet curAnnSet = defaultAnnotSet.get("NameFinder");
        if (curAnnSet.iterator().hasNext()) { // only one name will be
            // found.
            Annotation currAnnot = (Annotation) curAnnSet.iterator().next();
            String gender = (String) currAnnot.getFeatures().get("gender");
            if (gender != null && gender.length() > 0) {
                profileJSON.put("gender", gender);
            }

            // Needed name Features
            JSONObject nameJson = new JSONObject();
            String[] nameFeatures = new String[] { "firstName",
                    "middleName", "surname" };
            for (String feature : nameFeatures) {
                String s = (String) currAnnot.getFeatures().get(feature);
                if (s != null && s.length() > 0) {
                    nameJson.put(feature, s);
                }
            }
            profileJSON.put("name", nameJson);
        } // name

        // title
        curAnnSet = defaultAnnotSet.get("TitleFinder");
        if (curAnnSet.iterator().hasNext()) { // only one title will be
            // found.
            Annotation currAnnot = (Annotation) curAnnSet.iterator().next();
            String title = stringFor(doc, currAnnot);
            if (title != null && title.length() > 0) {
                profileJSON.put("title", title);
            }
        }// title

        // email,address,phone,url
        String[] annSections = new String[] { "EmailFinder", "AddressFinder", "PhoneFinder", "URLFinder" };
        String[] annKeys = new String[] { "email", "address", "phone", "url" };
        for (short i = 0; i < annSections.length; i++) {
            String annSection = annSections[i];
            curAnnSet = defaultAnnotSet.get(annSection);
            Iterator it = curAnnSet.iterator();
            JSONArray sectionArray = new JSONArray();
            while (it.hasNext()) { // extract all values for each
                // address,email,phone etc..
                Annotation currAnnot = (Annotation) it.next();
                String s = stringFor(doc, currAnnot);
                if (s != null && s.length() > 0) {
                    sectionArray.add(s);
                }
            }
            if (sectionArray.size() > 0) {
                profileJSON.put(annKeys[i], sectionArray);
            }
        }

        return profileJSON;


    }


    public static void main(String[] args) {

        String inputFileName = "data/cv/Alvin_Indeed.pdf";

        Path p = Paths.get(inputFileName);
        String filename = p.getFileName().toString();
        String outputFileName = "data/output/"+ FilenameUtils.removeExtension(filename) +".json";
        try {
            File tikkaConvertedFile = parseToHTMLUsingApacheTikka(inputFileName);
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
