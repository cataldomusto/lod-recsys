package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static di.uniba.it.lodrecsys.graph.RecommenderSys.featureSelection;
import static di.uniba.it.lodrecsys.graph.RecommenderSys.loadValue;

/**
 * Created by simo on 06/01/15.
 */
public class GraphFSRun {
    public static void main(String[] args) throws IOException {

        System.out.println(new Date() + "[INFO] Feature Started.");

        loadValue();

        for (SparsityLevel level : SparsityLevel.values()) {
            for (int numSplit = 1; numSplit <= LoadProperties.NUMSPLIT; numSplit++) {

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
            }
        }
        System.out.println(new Date() + "[INFO] Feature Completed.");
    }
}
