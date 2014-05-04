package di.uniba.it.lodrecsys;

import di.uniba.it.lodrecsys.baseline.IIRecSys;
import di.uniba.it.lodrecsys.baseline.MatrixFact;
import di.uniba.it.lodrecsys.baseline.UURecSys;
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

        /**
         *
         * Misure: valuteremo al momento precision e recall delle liste di top-N recommendations,
         * al variare di N (5, 10, 15, 20)
         - User to user e Item to Item Collaborative Filtering: neighbors (20, 50, 80)
         - BPRMF: numero di fattori latenti:  10, 20, 50, 100
         *
         *
         * */

        List<Map<String, String>> metricsForSplit = new ArrayList<>();
        String[] rec_methods = {"ItemKNN", "UserKNN", "BPRMF"};
        int[] list_rec_size = new int[]{5, 10, 15, 20};
        String methodOptions = "--recommender-options=";
        int numberOfSplit = 5;

        for (String method : rec_methods) {
            for (int num_rec : list_rec_size) {

                if (method.equals(IIRecSys.algorithmName) || method.equals(UURecSys.algorithmName)) {
                    for (int num_neigh : IIRecSys.num_neighbors) {
                        // for each sparsity level
                        String neigh_options = "\"k=" + num_neigh + "\"";
                        for (SparsityLevel level : SparsityLevel.values()) {
                            //for each split (from 1 to 5)
                            for (int i = 1; i <= 5; i++) {
                                String trainFile = trainPath + File.separator + "given_" + level.toString() + File.separator +
                                        "u" + i + ".base",
                                        testFile = testPath + File.separator + "u" + i + ".test",
                                        trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
                                        resFile = resPath + File.separator + method + File.separator + "neigh_" + num_neigh + File.separator + "given_" + level.toString() + File.separator +
                                                "u" + i + "_" + level.toString() + ".mml_res",
                                        trecResFile = resPath + File.separator + method + File.separator + "neigh_" + num_neigh + File.separator + "given_" + level.toString() + File.separator +
                                                "u" + i + "_" + level.toString() + ".results";

                                // Executes MyMediaLite tool
                                String mmlString = "item_recommendation --training-file=" + trainFile + " --test-file=" +
                                        testFile + " --prediction-file=" + resFile +
                                        " --recommender=" + method + " --overlap-items --predict-items-number="
                                        + num_rec + " " + methodOptions + neigh_options;
                                currLogger.info(mmlString);
                                CmdExecutor.executeCommand(mmlString, false);

                                // Now transform the results file in the TrecEval format for evaluation
                                EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
                                String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                                        + File.separator + "u" + i + ".final";
                                EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
                                metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                                currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                            }

                            currLogger.info(("Metrics results for sparsity level " + level + " is \n" + EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit)));
                            metricsForSplit.clear(); // evaluate for the next sparsity level
                        }

                    }
                } else {

                    for (int latent_fact : MatrixFact.latent_factors) {
                        String fact_options = "\"num_factors=\"" + latent_fact;
                        // for each sparsity level
                        for (SparsityLevel level : SparsityLevel.values()) {
                            //for each split (from 1 to 5)
                            for (int i = 1; i <= 5; i++) {
                                String trainFile = trainPath + File.separator + "given_" + level.toString() + File.separator +
                                        "u" + i + ".base",
                                        testFile = testPath + File.separator + "u" + i + ".test",
                                        trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
                                        resFile = resPath + File.separator + method + File.separator + "fact_" + latent_fact + File.separator + "given_" + level.toString() + File.separator +
                                                "u" + i + "_" + level.toString() + ".mml_res",
                                        trecResFile = resPath + File.separator + method + File.separator + "fact_" + latent_fact + File.separator + "given_" + level.toString() + File.separator +
                                                "u" + i + "_" + level.toString() + ".results";


                                // Executes MyMediaLite tool
                                String mmlString = "item_recommendation --training-file=" + trainFile + " --test-file=" +
                                        testFile + " --prediction-file=" + resFile + " --recommender=ItemKNN --overlap-items --predict-items-number=" + num_rec + " " + methodOptions + fact_options;
                                ;
                                currLogger.info(mmlString);
                                CmdExecutor.executeCommand(mmlString, false);

                                // Now transform the results file in the TrecEval format for evaluation
                                EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
                                String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                                        + File.separator + "u" + i + ".final";
                                EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
                                metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                                currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                            }

                            currLogger.info(("Metrics results for sparsity level " + level + " is \n" + EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit)));
                            metricsForSplit.clear(); // evaluate for the next sparsity level
                        }
                    }

                }

            }
        }

    }

}
