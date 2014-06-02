package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.CmdExecutor;
import di.uniba.it.lodrecsys.utils.PredictionFileConverter;
import di.uniba.it.lodrecsys.utils.Utils;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.util.Graphs;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by asuglia on 5/21/14.
 */
public class GraphRunner {
    private static Logger currLogger = Logger.getLogger(GraphRunner.class.getName());

    public static void main(String[] args) throws IOException, TasteException {
        String trainPath = "/home/asuglia/thesis/dataset/ml-100k/definitive",
                testPath = "/home/asuglia/thesis/dataset/ml-100k/binarized",
                testTrecPath = "/home/asuglia/thesis/dataset/ml-100k/trec",
                resPath = "/home/asuglia/thesis/dataset/ml-100k/results/TEST";

        String trainSet = testPath + File.separator + "u1.base", // given all
                testSet = testPath + File.separator + "u1.test";
        String resFile = resPath + File.separator + "test.results";

        UserItemPriorGraph userItemGraph = new UserItemPriorGraph(trainSet, testSet);

        int numRec = 10;
        double massProb = 0.8;

        userItemGraph.runPageRank(resFile, new RequestStruct(numRec, massProb));
        String trecResultFinal = resFile.substring(0, resFile.lastIndexOf(File.separator))
                + File.separator + "u1_" + numRec + "_" + massProb + ".final";
        EvaluateRecommendation.saveTrecEvalResult(testTrecPath + File.separator + "u1.test", resFile, trecResultFinal);
        Map<String, String> metrics = EvaluateRecommendation.getTrecEvalResults(trecResultFinal);

        System.out.println(metrics);

        //userItemGraph.generateGraph(trainSet);

//        String[] graphMethods = new String[]{"UserItemGraph", "UserItemPriorGraph"};//new String[]{"UserItemGraph", "UserItemTAGME", "UserItemLOD", "UserItemTL"};
//        List<Map<String, String>> metricsForSplit = new ArrayList<>();
//        //String method = graphMethods[0];
//        //int numRec = 5;
//        int[] listRecSizes = new int[]{5, 10, 15, 20};
//        int numberOfSplit = 5;
//
//        for (String method : graphMethods) {
//            for (SparsityLevel level : SparsityLevel.values()) {
//                for (int numRec : listRecSizes) {
//                    //for each split (from 1 to 5)
//                    String completeResFile = resPath + File.separator + method + File.separator + "given_" + level.toString() + File.separator +
//                            "top_" + numRec + File.separator + "metrics.complete";
//
//                    for (int i = 1; i <= numberOfSplit; i++) {
//                        String trainFile = trainPath + File.separator + "given_" + level.toString() + File.separator +
//                                "u" + i + ".base",
//                                testFile = testPath + File.separator + "u" + i + ".test",
//                                trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
//                                resFile = resPath + File.separator + method + File.separator + "given_" + level.toString() + File.separator +
//                                        "top_" + numRec + File.separator + "u" + i + ".results";
//
//
//                        // Now transform the results file in the TrecEval format for evaluation
//                        RecGraph userItemGraph = null;
//                        if (method.equals("UserItemGraph"))
//                            userItemGraph = new UserItemGraph(trainFile);
//                        else
//                            userItemGraph = new UserItemPriorGraph(trainFile);
//
//                        DataModel testModel = new FileDataModel(new File(testFile));
//
//                        userItemGraph.runPageRank(resFile, new RequestStruct(testModel, numRec));
//                        String trecResultFinal = resFile.substring(0, resFile.lastIndexOf(File.separator))
//                                + File.separator + "u" + i + ".final";
//                        EvaluateRecommendation.saveTrecEvalResult(trecTestFile, resFile, trecResultFinal);
//                        metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
//                        currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
//                    }
//
//                    currLogger.info(("Metrics results for sparsity level " + level + "\n"));
//                    EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit), completeResFile);
//                    metricsForSplit.clear(); // evaluate for the next sparsity level
//                }
//            }
//
//
//        }
    }


}
