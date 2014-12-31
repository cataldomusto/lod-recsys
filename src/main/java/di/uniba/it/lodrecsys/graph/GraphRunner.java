package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.File;
import java.io.IOException;
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

            for (int numSplit = 1; numSplit <= LoadProperties.NUMSPLIT; numSplit++) {

                String trainFile = LoadProperties.TRAINPATH + File.separator +
                        level + File.separator +
                        "u" + numSplit + ".base";

                String testFile = LoadProperties.TESTPATH + File.separator +
                        "u" + numSplit + ".test";

                featureSelection(trainFile, testFile);

                System.out.println("***************************************************");
                System.out.println("***    Recommender with pagerank algorithm      ***");
                System.out.println("***************************************************");
                System.out.println();
                System.out.println(new Date() + " [INFO] Inizialized computing recommendations for split #" + numSplit + " level: " + level + " ...");

                recommendations(trainFile, testFile);
                //        LOGGERGRAPHRUNNER.info("Computed recommendations for split #" + numSplit + " level: " + level);
                System.out.println(new Date() + " [INFO] Computed recommendations for split #" + numSplit + " level: " + level);
                System.out.println("-----------------------------------------------------");

            }

            evaluator(level);
        }

    }
}
