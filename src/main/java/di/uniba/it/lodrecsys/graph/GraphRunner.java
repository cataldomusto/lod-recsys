package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.*;
import java.util.Date;

import static di.uniba.it.lodrecsys.graph.RecommenderSys.*;

/**
 * Starts all the graph-based experiments and evaluate them
 * according to the trec_eval program.
 */
public class GraphRunner {

    public static void main(String[] args) throws IOException {

        loadValue();

        for (SparsityLevel level : SparsityLevel.values()) {
//            new RecommenderSys(level.toString()).start();
//            for (int numSplit = 1; numSplit <= LoadProperties.NUMSPLIT; numSplit++) {
//
////            SplitThread e = new SplitThread(level, numSplit);
////            e.start();
////            while (e.isAlive()){
////            }
//
//                String trainFile = LoadProperties.TRAINPATH + File.separator +
//                        level + File.separator +
//                        "u" + numSplit + ".base";
//
//                String testFile = LoadProperties.TESTPATH + File.separator +
//                        "u" + numSplit + ".test";
//
//                try {
//                    featureSelection(trainFile, testFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                savefileLog("***************************************************");
//                savefileLog("***    Recommender with pagerank algorithm      ***");
//                savefileLog("***************************************************");
//                savefileLog("");
//                savefileLog(new Date() + " [INFO] Inizialized computing recommendations for split #" + numSplit + " level: " + level + " ...");
//
//                try {
//                    recommendations(trainFile, testFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                //        LOGGERGRAPHRUNNER.info("Computed recommendations for split #" + numSplit + " level: " + level);
//                savefileLog(new Date() + " [INFO] Computed recommendations for split #" + numSplit + " level: " + level);
//                savefileLog("-----------------------------------------------------");
//            }
//
//            try {
//                saveRec(level.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            try {
                evaluator(level.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    public static void savefileLog(String s) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("./sperimentazione", true)))) {
            out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(s);
    }
}
