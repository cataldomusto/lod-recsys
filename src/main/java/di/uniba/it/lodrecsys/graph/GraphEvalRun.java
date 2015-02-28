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
        String fileProp = LoadProperties.FILTERTYPE = args[1];
        if (!LoadProperties.FILTERTYPE.equals("CFSubsetEval")) {
            LoadProperties.NUMFILTER = args[2];
            fileProp += args[2];
        }
        if (LoadProperties.FILTERTYPE.equals("RankerWeka")) {
            LoadProperties.EVALWEKA = args[3];
            fileProp += args[3];
        }

        LoadProperties.CHOOSENPROP = LoadProperties.MAPPINGPATH + "/choosen_prop/choosen_prop" + fileProp;

        System.out.print(new Date() + "[INFO] Started evaluation ");
        for (String arg : args) {
            System.out.print(arg + " ");
        }
        System.out.println();

        boolean novelty = false;
        boolean diversity = false;
        boolean serendipity = false;

        for (String arg : args) {
            String par = arg.toLowerCase();
            if (par.contains("novelty"))
                novelty = true;
            if (par.contains("diversity"))
                diversity = true;
            if (par.contains("serendipity"))
                serendipity = true;
        }

        evaluator(level, novelty, diversity, serendipity);

        System.out.print(new Date() + "[INFO] Completed evaluation ");
        for (String arg : args) {
            System.out.print(arg + " ");
        }
        System.out.println("\n");


//        if (new File(LoadProperties.CHOOSENPROP).exists())
//            new File(LoadProperties.CHOOSENPROP).delete();
    }
}

