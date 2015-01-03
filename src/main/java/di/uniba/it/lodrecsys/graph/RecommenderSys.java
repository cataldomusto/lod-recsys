package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import static di.uniba.it.lodrecsys.graph.GraphRunner.savefileLog;

/**
 * Created by simo on 31/12/14.
 */
public class RecommenderSys extends Thread implements Serializable {

    String level;

    public RecommenderSys(String s) {
        super(s);
        level = s;
    }

    private static Logger LOGGERGRAPHRUNNER = Logger.getLogger(GraphRunner.class.getName());
    private static List<Map<String, Set<Rating>>> recommendationForSplits = new ArrayList<>();
    private static List<Map<String, String>> metricsForSplit = new ArrayList<>();
    private static List<MovieMapping> mappingList;
    private static Map<String, List<String>> tagmeConcepts;

    public static void loadValue() throws IOException {
        mappingList = Utils.loadDBpediaMappedItems(LoadProperties.MAPPEDITEMFILE);
//        tagmeConcepts = Utils.loadTAGmeConceptsForItems(LoadProperties.TAGMEDIR);
    }

    public static void featureSelection(String trainFile, String testFile) throws IOException {

//      Execute all algorithm of feature selection
        GraphFactory.createAllFeatureSelection(trainFile, testFile, LoadProperties.PROPERTYINDEXDIR, mappingList);

//      Create Graph to filter
        GraphFactory.createSubsetFeature(LoadProperties.FILTERTYPE, trainFile, testFile, LoadProperties.PROPERTYINDEXDIR, mappingList);

        //Copy n-properties to graph
        GraphFactory.subsetProp();
    }

    public static void recommendations(String trainFile, String testFile) throws IOException {
        //               Create Graph with subset of feature
        Pair<RecGraph, RequestStruct> pair = GraphFactory.create(LoadProperties.METHOD, trainFile,
                testFile, LoadProperties.MASSPROB, LoadProperties.PROPERTYINDEXDIR, mappingList, tagmeConcepts);
        RecGraph userItemGraph = pair.key;
        RequestStruct requestStruct = pair.value;

        recommendationForSplits.add(userItemGraph.runPageRank(requestStruct));
    }

    public static void saveRec(String level) throws IOException {
        String dir;
        if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + File.separator +
                    level;
        else
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + File.separator +
                    level;
        new File("./" + dir).mkdirs();
        FileOutputStream fos = new FileOutputStream("./" + dir + "/recommendationForSplits.bin");
        ObjectOutputStream o = new ObjectOutputStream(fos);
        o.writeObject(recommendationForSplits);
        o.close();
        fos.close();
    }

    public static void loadRec(String level) throws IOException, ClassNotFoundException {
        String dir;
        if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + File.separator +
                    level;
        else
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + File.separator +
                    level;
        FileInputStream fis = new FileInputStream("./" + dir + "/recommendationForSplits.bin");
        ObjectInputStream ois = new ObjectInputStream(fis);
        recommendationForSplits = (List<Map<String, Set<Rating>>>) ois.readObject();
        ois.close();
        fis.close();
    }

    private static void delSerRec(String level) {
        String dir;
        if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + File.separator +
                    level;
        else
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + File.separator +
                    level;
        File serRec = new File("./" + dir + "/recommendationForSplits.bin");
        if (serRec.exists())
            serRec.delete();
    }

    public static void evaluator(String level) throws IOException, ClassNotFoundException {
        loadRec(level);
        for (int numRec : LoadProperties.LISTRECSIZES) {
            String namePath;
            if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
                namePath = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + File.separator +
                        level + File.separator +
                        "top_" + numRec;
            else
                namePath = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + File.separator +
                        level + File.separator +
                        "top_" + numRec;

            File f = new File(namePath);
            f.mkdirs();
            String completeResFile = namePath + File.separator + "metrics.complete";
            for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                String trecTestFile = LoadProperties.TESTTRECPATH + File.separator + "u" + i + ".test";
                String resFile = namePath + File.separator + "u" + i + ".results";

                EvaluateRecommendation.serializeRatings(recommendationForSplits.get(i - 1), resFile, numRec);

                String trecResultFinal = resFile.substring(0, resFile.lastIndexOf(File.separator))
                        + File.separator + "u" + i + ".final";
                EvaluateRecommendation.saveTrecEvalResult(trecTestFile, resFile, trecResultFinal);
                metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                LOGGERGRAPHRUNNER.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
            }

            LOGGERGRAPHRUNNER.info(("Metrics results for sparsity level " + level + "\n"));
            EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, LoadProperties.NUMSPLIT), completeResFile);
            metricsForSplit.clear(); // evaluate for the next sparsity level

        }
        recommendationForSplits.clear();
        delSerRec(level);
    }

    @Override
    public void run() {
        for (int numSplit = 1; numSplit <= LoadProperties.NUMSPLIT; numSplit++) {

//            SplitThread e = new SplitThread(level, numSplit);
//            e.start();
//            while (e.isAlive()){
//            }

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
        try {
            evaluator(level);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
