package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static di.uniba.it.lodrecsys.graph.GraphRunner.savefileLog;
import static di.uniba.it.lodrecsys.graph.RecommenderSys.featureSelection;
import static di.uniba.it.lodrecsys.graph.RecommenderSys.recommendations;

/**
 * Created by simo on 31/12/14.
 */
public class SplitThread extends Thread {

    String level;
    int numSplit;

    public SplitThread(String leveldo, int split) {
        super();
        level = leveldo;
        numSplit = split;
    }

    @Override
    public void run() {
        String trainFile = LoadProperties.TRAINPATH + File.separator +
                level + File.separator +
                "u" + numSplit + ".base";

        String testFile = LoadProperties.TESTPATH + File.separator +
                "u" + numSplit + ".test";

        try {
            featureSelection(trainFile, testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        savefileLog("***************************************************");
        savefileLog("***    Recommender with pagerank algorithm      ***");
        savefileLog("***************************************************");
        savefileLog("");
        savefileLog(new Date() + " [INFO] Inizialized computing recommendations for split #" + numSplit + " level: " + level + " ...");

        try {
            recommendations(trainFile, testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //        LOGGERGRAPHRUNNER.info("Computed recommendations for split #" + numSplit + " level: " + level);
        savefileLog(new Date() + " [INFO] Computed recommendations for split #" + numSplit + " level: " + level);
        savefileLog("-----------------------------------------------------");

    }
}
