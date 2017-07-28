package ai.deepfit.engine.segmentation;

/**
 * Created by alvinjin on 2017-07-01.
 */
import java.io.*;

import gate.*;
import gate.util.*;
import gate.util.persistence.PersistenceManager;

/**
 * This class illustrates how to use ANNIE as a sausage machine
 * in another application - put ingredients in one end (URLs pointing
 * to documents) and get sausages (e.g. Named Entities) out the
 * other end.
 * <P><B>NOTE:</B><BR>
 * For simplicity's sake, we don't do any exception handling.
 */
public class Annie  {

    /** The Corpus Pipeline application to contain ANNIE */
    private CorpusController annieController;

    /**
     * Initialise the ANNIE system. This creates a "corpus pipeline"
     * application that can be used to run sets of documents through
     * the extraction system.
     */
    public void initAnnie() throws GateException, IOException {
        Out.prln("Initialising processing engine...");

        // load the ANNIE application from the saved state in plugins/ANNIE
        File gateHome = Gate.getGateHome();
        File annieGapp = new File(gateHome, "ANNIEResumeParser.gapp");
        annieController = (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);

    }

    /** Tell ANNIE's controller about the corpus you want to run on */
    public void setCorpus(Corpus corpus) {
        annieController.setCorpus(corpus);
    }

    /** Run ANNIE */
    public void execute() throws GateException {
        Out.prln("Running processing engine...");
        annieController.execute();
        Out.prln("...processing engine complete");
    }
}
