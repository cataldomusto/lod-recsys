package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static di.uniba.it.lodrecsys.graph.RecommenderSys.evaluator;

/**
 * Created by simo on 06/01/15.
 */

//java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given MRMR 30

public class GraphEvalRun {

    public static void main(String[] args) throws IOException {

        String level = args[0];
        String fileProp = LoadProperties.FILTERTYPE = args[1];
        if (!LoadProperties.FILTERTYPE.equals("CFSubsetEval")) {
            LoadProperties.NUMFILTER = args[2];
            fileProp += args[2];
        }
        if (LoadProperties.FILTERTYPE.equals("RankerWeka")) {
            LoadProperties.EVALWEKA = args[3];
            fileProp += args[3];
        }

        LoadProperties.CHOOSENPROP = "./mapping/choosen_prop" + fileProp;

        System.out.println(new Date() + "[INFO] Evaluation " + level + " Started.");

        evaluator(level);

        System.out.println(new Date() + "[INFO] Evaluation " + level + " Completed.");

        if (new File(LoadProperties.CHOOSENPROP).exists())
            new File(LoadProperties.CHOOSENPROP).delete();
    }
}

