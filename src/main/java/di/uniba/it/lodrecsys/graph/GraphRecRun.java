package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.*;
import java.util.Date;

import static di.uniba.it.lodrecsys.graph.RecommenderSys.*;

/**
 * Created by simo on 06/01/15.
 */
public class GraphRecRun {

    private static String level;

    public static void main(String[] args) throws IOException {

        level = args[0];
        loadValue();

        System.out.println(new Date() + "[INFO] Recommendation " + level + " Started.");

//        for (SparsityLevel level : SparsityLevel.values()) {
        for (int numSplit = 1; numSplit <= LoadProperties.NUMSPLIT; numSplit++) {

            String trainFile = LoadProperties.TRAINPATH + File.separator +
                    level + File.separator +
                    "u" + numSplit + ".base";

            String testFile = LoadProperties.TESTPATH + File.separator +
                    "u" + numSplit + ".test";

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

        try {
            saveRec(level);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(new Date() + "[INFO] Recommendation " + level + " Completed.");
    }

    public static void cleanfileLog() {
//        new File("./datasets/ml-100k/results/UserItemExpDBPedia/"+LoadProperties.FILTERTYPE+"/log/").mkdirs();
        new File("./datasets/ml-100k/results/UserItemExpDBPedia/" + LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split/log/sperimentazione" + level).delete();
    }

    public static void savefileLog(String s) {
        new File("./datasets/ml-100k/results/UserItemExpDBPedia/" + LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split/log/").mkdirs();
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("./datasets/ml-100k/results/UserItemExpDBPedia/" + LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split/log/sperimentazione" + level, true)))) {
            out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(s);
    }
}