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

    //java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_10 MRMR 30

    public static void main(String[] args) throws IOException {

        level = args[0];
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

        if (!new File(LoadProperties.CHOOSENPROP).exists())
            GraphFactory.subsetProp();

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
        String dir;
        switch (LoadProperties.FILTERTYPE) {
            case "RankerWeka":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator;
                break;
            case "CFSubsetEval":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator;
                break;
            default:
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator;
                break;
        }
//        new File("./datasets/ml-100k/results/UserItemExpDBPedia/"+LoadProperties.FILTERTYPE+"/log/").mkdirs();
        new File(dir + "/log/sperimentazione" + level).delete();
    }

    public static void savefileLog(String s) {
        String dir;
        switch (LoadProperties.FILTERTYPE) {
            case "RankerWeka":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator;
                break;
            case "CFSubsetEval":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator;
                break;
            default:
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator;
                break;

        }

        new File(dir + "/log/").mkdirs();
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir + "/log/sperimentazione" + level, true)))) {
            out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(s);
    }
}