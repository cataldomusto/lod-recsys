package di.uniba.it.lodrecsys.baseline;

import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.utils.CmdExecutor;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import di.uniba.it.lodrecsys.utils.PredictionFileConverter;

import java.io.*;
import java.util.*;

import static di.uniba.it.lodrecsys.eval.EvaluateRecommendation.mapFilmCount;


/**
 * Created by Simone Rutigliano on 11/04/15.
 */

//java -cp lodrecsys.jar di.uniba.it.lodrecsys.baseline.Baseline movielens given_5 UserKNN novelty diversity

public class Baseline {

    private static List<Map<String, Set<Rating>>> recommendationForSplits = new ArrayList<>();
    private static List<Map<String, String>> metricsForSplit = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        LoadProperties.init(args[0]);
        String level = args[1];
        String method = args[2];

        System.out.print(new Date() + "[INFO] Started baseline ");
        for (String arg : args) {
            System.out.print(arg + " ");
        }
        System.out.println();

        boolean novelty = false;
        boolean diversity = false;

        for (String arg : args) {
            String par = arg.toLowerCase();
            if (par.contains("novelty"))
                novelty = true;
            if (par.contains("diversity"))
                diversity = true;
        }

        base(method, novelty, diversity, level);

        System.out.print(new Date() + "[INFO] Completed evaluation ");
        for (String arg : args) {
            System.out.print(arg + " ");
        }
        System.out.println("\n");

    }

    private static void base(String method, boolean novelty, boolean diversity, String level) throws IOException {
        List<Integer> listRecSize = getIntList(LoadProperties.LISTRECSIZES);
        String dirLog = LoadProperties.RESPATH + File.separator + method;

        if (method.equals(IIRecSys.algorithmName) || method.equals(UURecSys.algorithmName))
            evalUUIIRecSys(listRecSize, method, dirLog, level, diversity, novelty);

        else if (method.equals(MatrixFact.algorithmName))
            BPRMF(listRecSize, method, dirLog, level, diversity, novelty);

        else notPersonalized(listRecSize, method, dirLog, level, diversity, novelty);

    }

    private static void notPersonalized(List<Integer> listRecSize, String method, String dirLog, String level, boolean diversity, boolean novelty) throws IOException {

        for (int numRec : listRecSize) {
            String completeResFile = LoadProperties.RESPATH + File.separator + method + File.separator + level + File.separator +
                    "top_" + numRec
                    + File.separator + "metrics.complete";
            for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                String trainFile = LoadProperties.TRAINPATH + File.separator + level + File.separator +
                        "u" + i + ".base",
                        testFile = LoadProperties.TESTPATH + File.separator + "u" + i + ".test",
                        trecTestFile = LoadProperties.TESTTRECPATH + File.separator + "u" + i + ".test",
                        resFile = LoadProperties.RESPATH + File.separator + method + File.separator + level + File.separator +
                                "top_" + numRec + File.separator + "u" + i + ".mml_res",
                        tempResFile = LoadProperties.RESPATH + File.separator + method + File.separator + level + File.separator +
                                "top_" + numRec + File.separator + "u" + i + ".temp_res",
                        trecResFile = LoadProperties.RESPATH + File.separator + method + File.separator + level + File.separator +
                                "top_" + numRec + File.separator + "u" + i + ".results";

                if (!new File(tempResFile).exists()) {
                    new File((tempResFile)).getParentFile().mkdirs();
                    new File(tempResFile).createNewFile();
                }

                // Executes MyMediaLite tool
                String mmlString = "./datasets/mymedialite310/bin/item_recommendation --training-file=" + trainFile + " --test-file=" +
                        testFile + " --prediction-file=" + tempResFile + " --recommender=" + method + " ";

//                            currLogger.info(mmlString);
                savelog(dirLog, mmlString);

                String logFile = new File((tempResFile)).getParentFile() + "/result.log";
                CmdExecutor.executeCommandAndPrintLinux(mmlString, logFile);


                // Now transform the results file in the TrecEval format for evaluation
                PredictionFileConverter.fixPredictionFile(testFile, tempResFile, resFile, numRec);
                EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
                String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                        + File.separator + "u" + i + ".final";
                EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
                recommendationForSplits.add(EvaluateRecommendation.extractPredictionFile(resFile));

//                CmdExecutor.executeCommand("rm -f " + tempResFile, false);
            }

//           // Diversity measure
            ArrayList<String> diversityMeasureAll = null, diversityMeasureAvg = null;
            if (diversity) {
                ArrayList<HashMap<String, HashMap<String, ArrayList<String>>>> mapFilmCountProp = mapFilmCount();
                diversityMeasureAll = new ArrayList<>(LoadProperties.NUMSPLIT);
                diversityMeasureAvg = new ArrayList<>(LoadProperties.NUMSPLIT);
                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    HashMap<String, String> measures = EvaluateRecommendation.evalILDMeasure(recommendationForSplits.get(i - 1), mapFilmCountProp);
                    diversityMeasureAll.add(measures.get("all"));
                    diversityMeasureAvg.add(measures.get("avg"));
                }
            }

//          Novelty measure
            ArrayList<String> noveltyMeasureAll = null, noveltyMeasureAvg = null;
            if (novelty) {
                noveltyMeasureAll = new ArrayList<>(LoadProperties.NUMSPLIT);
                noveltyMeasureAvg = new ArrayList<>(LoadProperties.NUMSPLIT);
                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    HashMap<String, String> measures = EvaluateRecommendation.evalMSIMeasure(recommendationForSplits.get(i - 1));
//                    noveltyMeasure.add(EvaluateRecommendation.evalMSIMeasure(recommendationForSplits.get(i - 1)));
                    noveltyMeasureAll.add(measures.get("all"));
                    noveltyMeasureAvg.add(measures.get("avg"));
                }
            }

            for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                String trecResFile = LoadProperties.RESPATH + File.separator + method + File.separator + level + File.separator +
                        "top_" + numRec + File.separator + "u" + i + ".results";
                String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                        + File.separator + "u" + i + ".final";
                if (diversity)
                    EvaluateRecommendation.saveEvalILDMeasure(diversityMeasureAvg.get(i - 1), trecResultFinal);
                if (novelty)
                    EvaluateRecommendation.saveEvalMSIMeasure(noveltyMeasureAvg.get(i - 1), trecResultFinal);

                metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
//                            currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                savelog(dirLog, metricsForSplit.get(metricsForSplit.size() - 1).toString());
            }

            savelog(dirLog, ("Metrics results for sparsity level " + level + "\n"));
//                        currLogger.info(("Metrics results for sparsity level " + level + "\n"));
            EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, LoadProperties.NUMSPLIT), completeResFile);
            metricsForSplit.clear(); // evaluate for the next sparsity level
        }
    }

    private static void BPRMF(List<Integer> listRecSize, String method, String dirLog, String level, boolean diversity, boolean novelty) throws IOException {

        for (int latentFact : MatrixFact.latentFactors) {

            for (int numRec : listRecSize) {
                String fact_options = "\"num_factors=" + latentFact + "\"";

                String completeResFile = LoadProperties.RESPATH + File.separator + method + File.separator + "fact_" + latentFact + File.separator + level + File.separator +
                        "top_" + numRec + File.separator + "metrics.complete";

                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    String trainFile = LoadProperties.TRAINPATH + File.separator + level + File.separator +
                            "u" + i + ".base",
                            testFile = LoadProperties.TESTPATH + File.separator + "u" + i + ".test",
                            trecTestFile = LoadProperties.TESTTRECPATH + File.separator + "u" + i + ".test",
                            resFile = LoadProperties.RESPATH + File.separator + method + File.separator + "fact_" + latentFact + File.separator + level + File.separator +
                                    "top_" + numRec + File.separator + "u" + i + ".mml_res",
                            tempResFile = LoadProperties.RESPATH + File.separator + method + File.separator + "fact_" + latentFact + File.separator + level + File.separator +
                                    "top_" + numRec + File.separator + "u" + i + ".temp_res",
                            trecResFile = LoadProperties.RESPATH + File.separator + method + File.separator + "fact_" + latentFact + File.separator + level + File.separator +
                                    "top_" + numRec + File.separator + "u" + i + ".results";


                    if (!new File(tempResFile).exists()) {
                        new File((tempResFile)).getParentFile().mkdirs();
                        new File(tempResFile).createNewFile();
                    }

                    String logFile = new File((tempResFile)).getParentFile() + "/result.log";
                    // Executes MyMediaLite tool
                    String mmlString = "./datasets/mymedialite310/bin/item_recommendation --training-file=" + trainFile + " --test-file=" +
                            testFile + " --prediction-file=" + tempResFile + " --recommender=" + method + " --recommender-options=" + fact_options;

//                                currLogger.info(mmlString);
                    savelog(dirLog, mmlString);
                    CmdExecutor.executeCommandAndPrintLinux(mmlString, logFile);


                    // Now transform the results file in the TrecEval format for evaluation
                    PredictionFileConverter.fixPredictionFile(testFile, tempResFile, resFile, numRec);
                    EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
                    String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                            + File.separator + "u" + i + ".final";
                    EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
                    recommendationForSplits.add(EvaluateRecommendation.extractPredictionFile(resFile));

//                    CmdExecutor.executeCommand("rm -f " + tempResFile, false);
                }

//               // Diversity measure
                ArrayList<String> diversityMeasureAll = null, diversityMeasureAvg = null;
                if (diversity) {
                    ArrayList<HashMap<String, HashMap<String, ArrayList<String>>>> mapFilmCountProp = mapFilmCount();
                    diversityMeasureAll = new ArrayList<>(LoadProperties.NUMSPLIT);
                    diversityMeasureAvg = new ArrayList<>(LoadProperties.NUMSPLIT);
                    for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                        HashMap<String, String> measures = EvaluateRecommendation.evalILDMeasure(recommendationForSplits.get(i - 1), mapFilmCountProp);
                        diversityMeasureAll.add(measures.get("all"));
                        diversityMeasureAvg.add(measures.get("avg"));
                    }
                }

//          Novelty measure
                ArrayList<String> noveltyMeasureAll = null, noveltyMeasureAvg = null;
                if (novelty) {
                    noveltyMeasureAll = new ArrayList<>(LoadProperties.NUMSPLIT);
                    noveltyMeasureAvg = new ArrayList<>(LoadProperties.NUMSPLIT);
                    for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                        HashMap<String, String> measures = EvaluateRecommendation.evalMSIMeasure(recommendationForSplits.get(i - 1));
//                    noveltyMeasure.add(EvaluateRecommendation.evalMSIMeasure(recommendationForSplits.get(i - 1)));
                        noveltyMeasureAll.add(measures.get("all"));
                        noveltyMeasureAvg.add(measures.get("avg"));
                    }
                }

                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    String trecResFile = LoadProperties.RESPATH + File.separator + method + File.separator + "fact_" + latentFact + File.separator + level + File.separator +
                            "top_" + numRec + File.separator + "u" + i + ".results";
                    String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                            + File.separator + "u" + i + ".final";
                    if (diversity)
                        EvaluateRecommendation.saveEvalILDMeasure(diversityMeasureAvg.get(i - 1), trecResultFinal);
                    if (novelty)
                        EvaluateRecommendation.saveEvalMSIMeasure(noveltyMeasureAvg.get(i - 1), trecResultFinal);
                    metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
//                                currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                    savelog(dirLog, metricsForSplit.get(metricsForSplit.size() - 1).toString());
                }

                savelog(dirLog, ("Metrics results for sparsity level " + level + "\n"));
                EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, LoadProperties.NUMSPLIT), completeResFile);
                metricsForSplit.clear(); // evaluate for the next sparsity level
            }
        }
    }

    private static void evalUUIIRecSys(List<Integer> listRecSize, String method, String dirLog, String level, boolean diversity, boolean novelty) throws IOException {

        for (int numNeigh : IIRecSys.numNeighbors) {

            for (int numRec : listRecSize) {

                String neigh_options = "\"k=" + numNeigh + "\"";
                String completeResFile = LoadProperties.RESPATH + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                        "top_" + numRec + File.separator + "metrics.complete";

                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    String trainFile = LoadProperties.TRAINPATH + File.separator + level + File.separator +
                            "u" + i + ".base",
                            testFile = LoadProperties.TESTPATH + File.separator + "u" + i + ".test",
                            trecTestFile = LoadProperties.TESTTRECPATH + File.separator + "u" + i + ".test",
                            tempResFile = LoadProperties.RESPATH + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                    "top_" + numRec + File.separator + "u" + i + ".temp_res",
                            resFile = LoadProperties.RESPATH + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                    "top_" + numRec + File.separator + "u" + i + ".mml_res",
                            trecResFile = LoadProperties.RESPATH + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                                    "top_" + numRec + File.separator + "u" + i + ".results";

                    if (!new File(tempResFile).exists()) {
                        new File((tempResFile)).getParentFile().mkdirs();
                        new File(tempResFile).createNewFile();
                    }

                    String logFile = new File((tempResFile)).getParentFile() + "/result.log";
                    // Executes MyMediaLite tool
                    String mmlString = "./datasets/mymedialite310/bin/item_recommendation --training-file=" + trainFile + " --test-file=" +
                            testFile + " --prediction-file=" + tempResFile +
                            " --recommender=" + method + " --recommender-options=" + neigh_options;
//                                currLogger.info(mmlString);
                    savelog(dirLog, mmlString);
                    CmdExecutor.executeCommandAndPrintLinux(mmlString, logFile);


                    // Now transform the results file in the TrecEval format for evaluation
                    PredictionFileConverter.fixPredictionFile(testFile, tempResFile, resFile, numRec);
                    EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
                    String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                            + File.separator + "u" + i + ".final";
                    EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, trecResultFinal);
                    recommendationForSplits.add(EvaluateRecommendation.extractPredictionFile(resFile));

//                    CmdExecutor.executeCommand("rm -f " + tempResFile, false);
                }

//               // Diversity measure
                ArrayList<String> diversityMeasureAll = null, diversityMeasureAvg = null;
                if (diversity) {
                    ArrayList<HashMap<String, HashMap<String, ArrayList<String>>>> mapFilmCountProp = mapFilmCount();
                    diversityMeasureAll = new ArrayList<>(LoadProperties.NUMSPLIT);
                    diversityMeasureAvg = new ArrayList<>(LoadProperties.NUMSPLIT);
                    for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                        HashMap<String, String> measures = EvaluateRecommendation.evalILDMeasure(recommendationForSplits.get(i - 1), mapFilmCountProp);
                        diversityMeasureAll.add(measures.get("all"));
                        diversityMeasureAvg.add(measures.get("avg"));
                    }
                }

//          Novelty measure
                ArrayList<String> noveltyMeasureAll = null, noveltyMeasureAvg = null;
                if (novelty) {
                    noveltyMeasureAll = new ArrayList<>(LoadProperties.NUMSPLIT);
                    noveltyMeasureAvg = new ArrayList<>(LoadProperties.NUMSPLIT);
                    for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                        HashMap<String, String> measures = EvaluateRecommendation.evalMSIMeasure(recommendationForSplits.get(i - 1));
//                    noveltyMeasure.add(EvaluateRecommendation.evalMSIMeasure(recommendationForSplits.get(i - 1)));
                        noveltyMeasureAll.add(measures.get("all"));
                        noveltyMeasureAvg.add(measures.get("avg"));
                    }
                }

                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    String trecResFile = LoadProperties.RESPATH + File.separator + method + File.separator + "neigh_" + numNeigh + File.separator + level + File.separator +
                            "top_" + numRec + File.separator + "u" + i + ".results";
                    String trecResultFinal = trecResFile.substring(0, trecResFile.lastIndexOf(File.separator))
                            + File.separator + "u" + i + ".final";
                    if (diversity) {
                        EvaluateRecommendation.saveEvalILDMeasure(diversityMeasureAvg.get(i - 1), trecResultFinal);
                    }
                    if (novelty)
                        EvaluateRecommendation.saveEvalMSIMeasure(noveltyMeasureAvg.get(i - 1), trecResultFinal);
                    metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                }

                savelog(dirLog, "Metrics results for sparsity level " + level + "\n");
                EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, LoadProperties.NUMSPLIT), completeResFile);
                metricsForSplit.clear(); // evaluate for the next sparsity level
                recommendationForSplits.clear();
            }
        }
    }

    private static List<Integer> getIntList(int[] stringArray) {
        List<Integer> list = new ArrayList<>();

        for (int s : stringArray)
            list.add(s);

        return list;
    }

    private static void savelog(String dir, String s) {
        new File(dir).mkdirs();
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir + "/sperimentazione.log", true)))) {
            out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}