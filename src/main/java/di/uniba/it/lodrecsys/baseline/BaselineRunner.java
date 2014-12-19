package di.uniba.it.lodrecsys.baseline;

import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.CmdExecutor;
import di.uniba.it.lodrecsys.utils.PredictionFileConverter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by asuglia on 4/4/14.
 */

/**
 * Defines the main class from which all the
 * baselines are launched and evaluated. This class supposes
 * that you have in your system PATH the MyMediaLite's item recommendation
 * tool in order to work properly.
 * <p/>
 * Also, in order to execute ItemKNNLod algorithm you need to have another
 * tool and
 */
public class BaselineRunner {

    private static Logger currLogger = Logger.getLogger(BaselineRunner.class.getName());

    private static List<Integer> getIntList(String[] stringArray) {
        List<Integer> list = new ArrayList<>();

        for (String s : stringArray)
            list.add(Integer.parseInt(s));

        return list;
    }

    /**
     * Execute all the baselines defined according to the property
     * file specified as arguments
     *
     * @param args The property's filename
     * @throws IOException an incorrect property file is specified
     */
    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
        prop.load(new FileReader(args[0]));

        /*
        EXAMPLE VALUES:
        String trainPath = "/home/asuglia/thesis/dataset/ml-100k/definitive",
                testPath = "/home/asuglia/thesis/dataset/ml-100k/binarized",
                testTrecPath = "/home/asuglia/thesis/dataset/ml-100k/trec",
                resPath = "/home/asuglia/thesis/dataset/ml-100k/results";
        */
        String trainPath = prop.getProperty("trainPath"),
                testPath = prop.getProperty("testPath"),
                testTrecPath = prop.getProperty("testTrecPath"),
                resPath = prop.getProperty("resPath");
//                itemKnnLodCmd = prop.getProperty("itemKnnLodCmd");


        List<Map<String, String>> metricsForSplit = new ArrayList<>();
        // EXAMPLE VALUE:
        String[] recMethods = {"ItemKNNLod", "UserKNN", "ItemKNN", "Random", "MostPopular", "BPRMF"};
//        String[] recMethods = prop.getProperty("recMethods").split(" ");
        // EXAMPLE VALUE:
        //int[] listRecSize = new int[]{5, 10, 15, 20};
        List<Integer> listRecSize = getIntList(prop.getProperty("listRecSize").split(" "));
        String methodOptions = "--recommender-options=";
        // EXAMPLE VALUE:
        // int numberOfSplit = 5;
        int numberOfSplit = Integer.parseInt(prop.getProperty("numberOfSplit"));


        for (String method : recMethods) {
            for (int numRec : listRecSize) {

                if (method.equals(IIRecSys.algorithmName) || method.equals(UURecSys.algorithmName)) {
                    for (int numNeigh : IIRecSys.numNeighbors) {
                        String neigh_options = "\"k=" + numNeigh + "\"";
                        // for each sparsity level
                        for (SparsityLevel level : SparsityLevel.values()) {
                            //for each split (from 1 to 5)
                            String completeResFile = resPath + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                    "top_" + numRec + File.separator + "metrics.complete";
                            for (int i = 1; i <= 5; i++) {
                                String trainFile = trainPath + File.separator + level + File.separator +
                                        "u" + i + ".base",
                                        testFile = testPath + File.separator + "u" + i + ".test",
                                        trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
                                        tempResFile = resPath + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                                "top_" + numRec + File.separator + "u" + i + ".temp_res",
                                        resFile = resPath + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                                "top_" + numRec + File.separator + "u" + i + ".mml_res",
                                        trecResFile = resPath + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                                "top_" + numRec + File.separator + "u" + i + ".results";


                                // Executes MyMediaLite tool
                                String mmlString = "item_recommendation --training-file=" + trainFile + " --test-file=" +
                                        testFile + " --prediction-file=" + tempResFile +
                                        " --recommender=" + method + " --recommender-options=" + neigh_options;
                                currLogger.info(mmlString);
                                CmdExecutor.executeCommand(mmlString, false);
                                // Now transform the results file in the TrecEval format for evaluation
                                PredictionFileConverter.fixPredictionFile(testFile, tempResFile, resFile, numRec);
                                EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
                                String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                                        + File.separator + "u" + i + ".final";
                                EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
                                metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                                currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                            }

                            currLogger.info(("Metrics results for sparsity level " + level + "\n"));
                            EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit), completeResFile);
                            metricsForSplit.clear(); // evaluate for the next sparsity level
                        }
                    }
                } else if (method.equals(MatrixFact.algorithmName)) {

                    for (int latentFact : MatrixFact.latentFactors) {
                        String fact_options = "\"num_factors=" + latentFact + "\"";
                        // for each sparsity level
                        for (SparsityLevel level : SparsityLevel.values()) {
                            //for each split (from 1 to 5)
                            String completeResFile = resPath + File.separator + method + File.separator + "fact_" + latentFact + File.separator + level + File.separator +
                                    "top_" + numRec + File.separator + "metrics.complete";
                            for (int i = 1; i <= 5; i++) {
                                String trainFile = trainPath + File.separator + level + File.separator +
                                        "u" + i + ".base",
                                        testFile = testPath + File.separator + "u" + i + ".test",
                                        trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
                                        resFile = resPath + File.separator + method + File.separator + "fact_" + latentFact + File.separator + level + File.separator +
                                                "top_" + numRec + File.separator + "u" + i + ".mml_res",
                                        tempResFile = resPath + File.separator + method + File.separator + "fact_" + latentFact + File.separator + level + File.separator +
                                                "top_" + numRec + File.separator + "u" + i + ".temp_res",
                                        trecResFile = resPath + File.separator + method + File.separator + "fact_" + latentFact + File.separator + level + File.separator +
                                                "top_" + numRec + File.separator + "u" + i + ".results";


                                // Executes MyMediaLite tool
                                String mmlString = "item_recommendation --training-file=" + trainFile + " --test-file=" +
                                        testFile + " --prediction-file=" + tempResFile + " --recommender=" + method + " --recommender-options=" + fact_options;

                                currLogger.info(mmlString);
                                CmdExecutor.executeCommand(mmlString, false);
                                // Now transform the results file in the TrecEval format for evaluation
                                PredictionFileConverter.fixPredictionFile(testFile, tempResFile, resFile, numRec);
                                EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
                                String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                                        + File.separator + "u" + i + ".final";
                                EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
                                metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                                currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                            }

                            currLogger.info(("Metrics results for sparsity level " + level + "\n"));
                            EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit), completeResFile);
                            metricsForSplit.clear(); // evaluate for the next sparsity level
                        }
                    }

                } else if (method.equals("ItemKNNLod")) { // Item-based CF with jaccard similarity matrix based on LOD properties
                    //String simPath = "/home/asuglia/thesis/dataset/ml-100k/results/ItemKNNLod/similarities";
                    String simPath = prop.getProperty("simPath");
                    for (int numNeigh : IIRecSys.numNeighbors) {
                        // for each sparsity level
                        for (SparsityLevel level : SparsityLevel.values()) {
                            //for each split (from 1 to 5)
                            String completeResFile = resPath + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                    "top_" + numRec + File.separator + "metrics.complete";
                            for (int i = 1; i <= 5; i++) {
                                String trainFile = trainPath + File.separator + level + File.separator +
                                        "u" + i + ".base",
                                        testFile = testPath + File.separator + "u" + i + ".test",
                                        trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
                                        tempResFile = resPath + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                                "top_" + numRec + File.separator + "u" + i + ".temp_res",
                                        resFile = resPath + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                                "top_" + numRec + File.separator + "u" + i + ".mml_res",
                                        trecResFile = resPath + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                                "top_" + numRec + File.separator + "u" + i + ".results",
                                        simFile = simPath + File.separator + level + File.separator + "u" + i + ".sim";


                                // Executes MyMediaLite tool
                                // itemknnLod command: /home/asuglia/itemlod_bin/itemknn_lod.exe
                                String itemKnnLodCmd = "/home/asuglia/itemlod_bin/itemknn_lod.exe";
                                String mmlString = "mono " + itemKnnLodCmd + " " + trainFile + " " +
                                        testFile + " " + simFile + " " + tempResFile + " " + numNeigh;
                                currLogger.info(mmlString);
                                CmdExecutor.executeCommand(mmlString, true);
                                // Now transform the results file in the TrecEval format for evaluation
                                PredictionFileConverter.fixPredictionFile(testFile, tempResFile, resFile, numRec);
                                EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
                                String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                                        + File.separator + "u" + i + ".final";
                                EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
                                metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                                currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                            }

                            currLogger.info(("Metrics results for sparsity level " + level + "\n"));
                            EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit), completeResFile);
                            metricsForSplit.clear(); // evaluate for the next sparsity level
                        }
                    }
                } else { // non-personalized algorithm
                    for (SparsityLevel level : SparsityLevel.values()) {
                        //for each split (from 1 to 5)
                        String completeResFile = resPath + File.separator + method + File.separator + level + File.separator +
                                "top_" + numRec
                                + File.separator + "metrics.complete";
                        for (int i = 1; i <= 5; i++) {
                            String trainFile = trainPath + File.separator + level + File.separator +
                                    "u" + i + ".base",
                                    testFile = testPath + File.separator + "u" + i + ".test",
                                    trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
                                    resFile = resPath + File.separator + method + File.separator + level + File.separator +
                                            "top_" + numRec + File.separator + "u" + i + ".mml_res",
                                    tempResFile = resPath + File.separator + method + File.separator + level + File.separator +
                                            "top_" + numRec + File.separator + "u" + i + ".temp_res",
                                    trecResFile = resPath + File.separator + method + File.separator + level + File.separator +
                                            "top_" + numRec + File.separator + "u" + i + ".results";


                            // Executes MyMediaLite tool
                            String mmlString = "item_recommendation --training-file=" + trainFile + " --test-file=" +
                                    testFile + " --prediction-file=" + tempResFile + " --recommender=" + method + " ";

                            currLogger.info(mmlString);
                            CmdExecutor.executeCommand(mmlString, false);
                            // Now transform the results file in the TrecEval format for evaluation
                            PredictionFileConverter.fixPredictionFile(testFile, tempResFile, resFile, numRec);
                            EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
                            String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                                    + File.separator + "u" + i + ".final";
                            EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
                            metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                            currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                        }

                        currLogger.info(("Metrics results for sparsity level " + level + "\n"));
                        EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit), completeResFile);
                        metricsForSplit.clear(); // evaluate for the next sparsity level
                    }

                }

            }
        }

    }

}
