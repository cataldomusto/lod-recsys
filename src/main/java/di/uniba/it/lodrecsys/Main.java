package di.uniba.it.lodrecsys;

import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.CmdExecutor;

import java.io.*;
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

        // for each sparsity level
        //for(SparsityLevel level : SparsityLevel.values()) {
        // for each split (from 1 to 5)
        SparsityLevel level = SparsityLevel.FIFTY;
        for (int i = 1; i <= 5; i++) {
            String trainFile = trainPath + File.separator + "given_" + level.toString() + File.separator +
                    "u" + i + "_" + level.toString() + ".base",
                    testFile = testPath + File.separator + "u" + i + ".test",
                    trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
                    resFile = resPath + File.separator + "given_" + level.toString() + File.separator +
                            "u" + i + "_" + level.toString() + ".mml_res",
                    trecResFile = resPath + File.separator + "given_" + level.toString() + File.separator +
                            "u" + i + "_" + level.toString() + ".results";

            // Executes MyMediaLite tool
            String mmlString = "item_recommendation --training-file=" + trainFile + " --test-file=" +
                    testFile + " --prediction-file=" + resFile + " --recommender=UserKNN";
            CmdExecutor.executeCommand(mmlString);

            // Now transform the results file in the TrecEval format for evaluation
            EvaluateRecommendation.generateTrecEvalFile(resFile, trecResFile);
            EvaluateRecommendation.saveTrecEvalResult(trecTestFile, trecResFile, i);
        }

        //}



    }

}
