package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.CmdExecutor;
import di.uniba.it.lodrecsys.utils.PredictionFileConverter;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.util.Graphs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by asuglia on 5/21/14.
 */
public class GraphRunner {
    private static Logger currLogger = Logger.getLogger(GraphRunner.class.getName());

    public static void main(String[] args) throws IOException {
        String trainPath = "/home/asuglia/thesis/dataset/ml-100k/definitive",
                testPath = "/home/asuglia/thesis/dataset/ml-100k/binarized",
                testTrecPath = "/home/asuglia/thesis/dataset/ml-100k/trec",
                resPath = "/home/asuglia/thesis/dataset/ml-100k/results";

        String trainSet = testPath + File.separator + "u1.base", // given all
               testSet = testPath + File.separator + "u1.test";
        UserItemGraph userItemGraph = new UserItemGraph(trainSet);

        PageRank<String, String> pageRank = new PageRank<>(userItemGraph.recGraph, 0.15);

        System.out.println(userItemGraph.recGraph.getVertexCount());
        System.out.println(userItemGraph.recGraph.getEdgeCount());

        pageRank.setMaxIterations(20);

        pageRank.evaluate();




        //userItemGraph.generateGraph(trainSet);


//        String[] graphMethods = new String []{"UserItem", "UserItemTAGME", "UserItemLOD", "UserItemTL"};
//        List<Map<String, String>> metricsForSplit = new ArrayList<>();
//        String method = graphMethods[0];
//        int numRec = 5;
//        int[] list_rec_size = new int[]{5, 10, 15, 20};
//        int numberOfSplit = 5;
//
//        for (SparsityLevel level : SparsityLevel.values()) {
//            //for each split (from 1 to 5)
//            String completeResFile = resPath + File.separator + method + File.separator + "neigh_" + num_neigh + File.separator + "given_" + level.toString() + File.separator +
//                    "top_" + numRec + File.separator + "metrics.complete";
//
//            for (int i = 1; i <= numberOfSplit; i++) {
//                String trainFile = trainPath + File.separator + "given_" + level.toString() + File.separator +
//                        "u" + i + ".base",
//                        testFile = testPath + File.separator + "u" + i + ".test",
//                        trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
//                        tempResFile = resPath + File.separator + method + File.separator + "given_" + level.toString() + File.separator +
//                                "top_" + numRec + File.separator + "u" + i + ".temp_res",
//                        resFile = resPath + File.separator + method + File.separator + "given_" + level.toString() + File.separator +
//                                "top_" + numRec + File.separator + "u" + i + ".mml_res",
//                        trecResFile = resPath + File.separator + method + File.separator + "given_" + level.toString() + File.separator +
//                                "top_" + numRec + File.separator + "u" + i + ".results";
//
//
//                // Now transform the results file in the TrecEval format for evaluation
//                PredictionFileConverter.fixPredictionFile(testFile, tempResFile, resFile, numRec);
//                EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
//                String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
//                        + File.separator + "u" + i + ".final";
//                EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
//                metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
//                currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
//            }
//
//            currLogger.info(("Metrics results for sparsity level " + level + "\n"));
//            EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit), completeResFile);
//            metricsForSplit.clear(); // evaluate for the next sparsity level
//        }


    }


}
