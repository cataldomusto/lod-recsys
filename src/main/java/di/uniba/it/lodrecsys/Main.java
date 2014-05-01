package di.uniba.it.lodrecsys;

import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.CmdExecutor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by asuglia on 4/4/14.
 */
public class Main {

    private static Logger currLogger = Logger.getLogger(Main.class.getName());


    public static void main(String[] args) throws IOException {
        String trainPath = "/home/asuglia/thesis/dataset/ml-100k/definitive",
                testPath = "/home/asuglia/thesis/dataset/ml-100k/binarized",
                testTrecPath = "/home/asuglia/thesis/dataset/ml-100k/trec",
                resPath = "/home/asuglia/thesis/dataset/ml-100k/results";

        List<Map<String, String>> metricsForSplit = new ArrayList<>();
        String[] rec_methods = {"ItemKNN", "UserKNN"};
        int[] rec_numbers = new int[]{5, 10, 15, 20};

        int numberOfSplit = 5;

        // for each sparsity level
        //for (SparsityLevel level : SparsityLevel.values()) {
        SparsityLevel level = SparsityLevel.ALL;
        //for each split (from 1 to 5)
            for (int i = 1; i <= 5; i++) {
                String trainFile = trainPath + File.separator + "given_" + level.toString() + File.separator +
                        "u" + i + ".base",
                        testFile = testPath + File.separator + "u" + i + ".test",
                        trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
                        resFile = resPath + File.separator + "given_" + level.toString() + File.separator +
                                "u" + i + "_" + level.toString() + ".mml_res",
                        trecResFile = resPath + File.separator + "given_" + level.toString() + File.separator +
                                "u" + i + "_" + level.toString() + ".results";

                // Executes MyMediaLite tool
                String mmlString = "item_recommendation --training-file=" + trainFile + " --test-file=" +
                        testFile + " --prediction-file=" + resFile + " --recommender=ItemKNN";
                currLogger.info(mmlString);
                CmdExecutor.executeCommand(mmlString, false);

                // Now transform the results file in the TrecEval format for evaluation
                EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
                String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                        + File.separator + "u" + i + ".final";
                EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
                metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
            }

        currLogger.info(("Metrics results for sparsity level " + level + " is \n" + EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit)));
        metricsForSplit.clear(); // evaluate for the next sparsity level
        //}

/*        String[] rec_methods = {"ItemKNN", "UserKNN"};
        int[] rec_numbers = new int[] {5,10,15,20};
        // for each split (from 1 to 5)

        for (String method : rec_methods) {
            for(int num_rec : rec_numbers) {
                for (int i = 1; i <= 5; i++) {

                    String trainFile = trainPath + File.separator + "given_" + level.toString() + File.separator +
                            "u" + i + ".base",
                            testFile = testPath + File.separator + "u" + i + ".test";

                    // Executes MyMediaLite tool
                    String mmlString = "item_recommendation --training-file=" + trainFile + " --test-file=" +
                            testFile + " --recommender=" + method + " --measures=prec@5,prec@10 " +
                            "--predict-items-number=" + num_rec;
                    System.out.println(mmlString);
                    //currLogger.info(mmlString);
                    CmdExecutor.executeCommand(mmlString, true);

                }
            }
        }
        */


    }

}
