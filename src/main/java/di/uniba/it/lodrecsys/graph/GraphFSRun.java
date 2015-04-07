package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static di.uniba.it.lodrecsys.graph.RecommenderSys.featureSelection;
import static di.uniba.it.lodrecsys.graph.RecommenderSys.loadValue;

/**
 * Created by Simone Rutigliano on 06/01/15.
 */

//java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun MRMR 30
//java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun CFSubsetEval
//java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun Custom
//java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun RankerWeka 30 PCA

public class GraphFSRun {

    public static void main(String[] args) throws IOException {

        String fileProp = LoadProperties.FILTERTYPE = args[0];
        if (LoadProperties.FILTERTYPE.contains("Custom")) {
            System.out.println(new Date() + "[INFO] Feature Completed with custom.");
            System.exit(1);
        }
        if (!LoadProperties.FILTERTYPE.equals("CFSubsetEval")) {
            LoadProperties.NUMFILTER = args[1];
            fileProp += args[1];
        }
        if (LoadProperties.FILTERTYPE.equals("RankerWeka")) {
            LoadProperties.EVALWEKA = args[2];
            fileProp += args[2];
        }

        LoadProperties.CHOOSENPROP = LoadProperties.MAPPINGPATH + "/choosen_prop/choosen_prop" + fileProp;
        new File(LoadProperties.MAPPINGPATH + "/choosen_prop/").mkdir();
        new File(LoadProperties.CHOOSENPROP).createNewFile();

        System.out.println(new Date() + "[INFO] Feature Started.");

        loadValue();

        String level = SparsityLevel.ALL.toString();
//        for (SparsityLevel level : SparsityLevel.values()) {
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
//        }
        System.out.println(new Date() + "[INFO] Feature Completed.");
    }
}
