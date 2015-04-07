package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import static di.uniba.it.lodrecsys.graph.RecommenderSys.*;

/**
 * Created by Simone Rutigliano on 06/01/15.
 */
public class GraphRecRun {

    private static String level;

    //java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_10 MRMR 30

    public static void main(String[] args) throws IOException {

        level = args[0];
        String fileProp = LoadProperties.FILTERTYPE = args[1];


        if (!LoadProperties.FILTERTYPE.contains("Custom")) {

            if (!LoadProperties.FILTERTYPE.equals("CFSubsetEval")) {
                LoadProperties.NUMFILTER = args[2];
                fileProp += args[2];
            }

            if (LoadProperties.FILTERTYPE.equals("RankerWeka")) {
                LoadProperties.EVALWEKA = args[3];
                fileProp += args[3];
            }
        }

        LoadProperties.CHOOSENPROP = LoadProperties.MAPPINGPATH + "/choosen_prop/choosen_prop" + fileProp;

        if (LoadProperties.FILTERTYPE.contains("Custom"))
            LoadProperties.NUMFILTER = dimFile(LoadProperties.CHOOSENPROP);

        if (!new File(LoadProperties.CHOOSENPROP).exists())
            GraphFactory.subsetProp();

        loadValue();

        System.out.println(new Date() + "[INFO] Recommendation " + level + " Started.");
        long startTime = System.currentTimeMillis();

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
        long endTime = System.currentTimeMillis();
        float minutes = (endTime - startTime) / (float) (1000 * 60);
        savefileLog("[INFO] Recommendation " + level + " completed in " + Math.round(minutes) + " minutes.");
    }


    private static String dimFile(String nameFile) {
        try {
            return Integer.toString(Files.readAllLines(Paths.get(nameFile), Charset.defaultCharset()).size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static void cleanfileLog() {
        String dir;
        if (LoadProperties.FILTERTYPE.equals("RankerWeka")) {
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator;

        } else if (LoadProperties.FILTERTYPE.equals("CFSubsetEval")) {
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator;

        } else if (LoadProperties.FILTERTYPE.contains("Custom")) {
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator;

        } else {
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator;

        }
//        new File("./datasets/ml-100k/results/UserItemExpDBPedia/"+LoadProperties.FILTERTYPE+"/log/").mkdirs();
        new File(dir + "/log/sperimentazione" + level).delete();
    }

    public static void savefileLog(String s) {
        String dir;
        if (LoadProperties.FILTERTYPE.equals("RankerWeka")) {
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator;

        } else if (LoadProperties.FILTERTYPE.equals("CFSubsetEval")) {
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator;

        } else if (LoadProperties.FILTERTYPE.contains("Custom")) {
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator;

        } else {
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator;

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