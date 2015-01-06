package di.uniba.it.lodrecsys.graph;

import java.io.IOException;
import java.util.Date;

import static di.uniba.it.lodrecsys.graph.RecommenderSys.evaluator;

/**
 * Created by simo on 06/01/15.
 */
public class GraphEvalRun {

    public static void main(String[] args) throws IOException {

        String level = args[0];

        System.out.println(new Date() + "[INFO] Evaluation " + level + " Started.");

        evaluator(level);

        System.out.println(new Date() + "[INFO] Evaluation " + level + " Completed.");
    }
}

