package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.utils.LoadProperties;

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
        LoadProperties.FILTERTYPE = args[1];
        LoadProperties.NUMFILTER = args[2];

        if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
            LoadProperties.EVALWEKA = args[3];

        System.out.println(new Date() + "[INFO] Evaluation " + level + " Started.");

        evaluator(level);

        System.out.println(new Date() + "[INFO] Evaluation " + level + " Completed.");
    }
}

