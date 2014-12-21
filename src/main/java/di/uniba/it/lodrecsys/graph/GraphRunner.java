package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Starts all the graph-based experiments and evaluate them
 * according to the trec_eval program.
 */
public class GraphRunner {
    private static Logger LOGGERGRAPHRUNNER = Logger.getLogger(GraphRunner.class.getName());
    static String TRAINPATH;
    static String TESTPATH;
    static String TESTTRECPATH;
    static String RESPATH;
    static String PROPERTYINDEXDIR;
    static String TAGMEDIR;
    static String MAPPEDITEMFILE;
    static String METHOD;
    static int[] LISTRECSIZES;
    static int NUMSPLIT;
    static double MASSPROB;

    /*
        String trainPath = "/home/asuglia/thesis/dataset/ml-100k/definitive",
                testPath = "/home/asuglia/thesis/dataset/ml-100k/binarized",
                testTrecPath = "/home/asuglia/thesis/dataset/ml-100k/trec",
                resPath = "/home/asuglia/thesis/dataset/ml-100k/results",
                propertyIndexDir = "/home/asuglia/thesis/content_lodrecsys/movielens/stored_prop",
                tagmeDir = "/home/asuglia/thesis/content_lodrecsys/movielens/tagme",
                mappedItemFile = "mapping/item.mapping";
        */

    static {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("properties/my.properties"));
            TRAINPATH = prop.getProperty("trainPath");
            TESTPATH = prop.getProperty("testPath");
            TESTTRECPATH = prop.getProperty("testTrecPath");
            RESPATH = prop.getProperty("resPath");
            PROPERTYINDEXDIR = prop.getProperty("propertyIndexDir");
            TAGMEDIR = prop.getProperty("tagmeDir");
            MAPPEDITEMFILE = prop.getProperty("mappedItemFile");
            METHOD = prop.getProperty("methodName");
        } catch (IOException e) {
            e.printStackTrace();
        }

        LISTRECSIZES = new int[]{5, 10, 15, 20};
        NUMSPLIT = 5;
        MASSPROB = 0.8;
    }

    public static void main(String[] args) throws IOException {

        List<MovieMapping> mappingList = Utils.loadDBpediaMappedItems(MAPPEDITEMFILE);
        Map<String, List<String>> tagmeConcepts = Utils.loadTAGmeConceptsForItems(TAGMEDIR);
        List<Map<String, String>> metricsForSplit = new ArrayList<>();

        List<Map<String, Set<Rating>>> recommendationForSplits = new ArrayList<>();

        for (SparsityLevel level : SparsityLevel.values()) {

            for (int i = 1; i <= NUMSPLIT; i++) {

                String trainFile = TRAINPATH + File.separator + level + File.separator +
                        "u" + i + ".base",
                        testFile = TESTPATH + File.separator + "u" + i + ".test";

                Pair<RecGraph, RequestStruct> pair = GraphFactory.create(METHOD, trainFile,
                        testFile, MASSPROB, PROPERTYINDEXDIR, mappingList, tagmeConcepts);
                RecGraph userItemGraph = pair.key;
                RequestStruct requestStruct = pair.value;

                recommendationForSplits.add(userItemGraph.runPageRank(requestStruct));
                LOGGERGRAPHRUNNER.info("Computed recommendations for split #" + i + " level: " + level);
            }


            for (int numRec : LISTRECSIZES) {
                File f = new File(RESPATH + File.separator + METHOD + File.separator + level + File.separator +
                        "top_" + numRec);
                f.mkdirs();
                String completeResFile = RESPATH + File.separator + METHOD + File.separator + level + File.separator +
                        "top_" + numRec + File.separator + "metrics.complete";
                for (int i = 1; i <= NUMSPLIT; i++) {
                    String trecTestFile = TESTTRECPATH + File.separator + "u" + i + ".test";
                    String resFile = RESPATH + File.separator + METHOD + File.separator + level + File.separator +
                            "top_" + numRec + File.separator + "u" + i + ".results";

                    EvaluateRecommendation.serializeRatings(recommendationForSplits.get(i - 1), resFile, numRec);
//
                    String trecResultFinal = resFile.substring(0, resFile.lastIndexOf(File.separator))
                            + File.separator + "u" + i + ".final";
                    EvaluateRecommendation.saveTrecEvalResult(trecTestFile, resFile, trecResultFinal);
                    metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                    LOGGERGRAPHRUNNER.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                }
//
                LOGGERGRAPHRUNNER.info(("Metrics results for sparsity level " + level + "\n"));
                EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, NUMSPLIT), completeResFile);
                metricsForSplit.clear(); // evaluate for the next sparsity level


            }

            recommendationForSplits.clear();
        }

    }
}
