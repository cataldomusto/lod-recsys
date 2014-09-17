package di.uniba.it.lodrecsys.eval;

import di.uniba.it.lodrecsys.utils.TrecEvalParser;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by asuglia on 5/17/14.
 */
public class SignificanceTest {
    public static double[] generateMeasureArray(Map<String, Map<String, Double>> usersMetrics, String measure) {
        double[] usersF1 = new double[usersMetrics.size()];

        int i = 0;

        for (String currUser : usersMetrics.keySet()) {
            Map<String, Double> currUserMetrics = usersMetrics.get(currUser);
            usersF1[i++] = currUserMetrics.get(measure);

        }
        return usersF1;

    }

    /**
     * Computes F1-measure for each user
     *
     * @param measures the metrics' map that will be updated with f1-measures
     */
    private static void evalF1Measure(Map<String, Float> measures, String cutoff) {

        String currPrecision = "P_" + cutoff,
                currRecall = "recall_" + cutoff;

        measures.put("F1_" + cutoff, EvaluateRecommendation.getF1(measures.get(currPrecision), measures.get(currRecall)));

    }

    private static List<String> readAlgorithmsPath(String algoListFileName) throws IOException {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(algoListFileName));
            List<String> algoNames = new ArrayList<>();

            String currAlgo = null;

            while ((currAlgo = reader.readLine()) != null) {
                algoNames.add(currAlgo);
            }

            return algoNames;
        } finally {
            if (reader != null)
                reader.close();
        }


    }

    private static double[] getAveragedMetricForUser(String resDir, String testDir, int numSplit, int totUsers, int reclistSize) throws IOException {
        double[] avgVector = new double[totUsers];
        Map<String, Float> userMetrics = new HashMap<>();
        String cutoff = String.valueOf(reclistSize);

        for (int i = 0; i < numSplit; i++) {
            String goldStandardFile = testDir + File.separator + "u" + i + ".test";
            String resFile = resDir + File.separator + "u" + i + ".results";
            String trecResultFinal = resFile.substring(0, resFile.lastIndexOf(File.separator))
                    + File.separator + "u" + i + ".final_users";
            EvaluateRecommendation.savePerUserTrec(goldStandardFile, resFile, trecResultFinal);
            Map<String, Map<String, Float>> perUserMetrics = TrecEvalParser.getPerUserMetrics(trecResultFinal);
            //
            for (String currUser : perUserMetrics.keySet()) {
                Map<String, Float> currMetrics = perUserMetrics.get(currUser);
                evalF1Measure(currMetrics, cutoff);
                userMetrics.put(currUser, userMetrics.getOrDefault(currUser, 0f) + currMetrics.get(currUser));
            }
        }

        for (String currUser : userMetrics.keySet()) {
            int userIndex = Integer.parseInt(currUser);
            avgVector[userIndex - 1] /= numSplit;
        }

        return avgVector;

    }

    private static Map<String, double[]> computeAllAlgoMetrics(List<String> algoList, String sparsityLevel, String testDir, int totUsers, int numSplit, int reclistSize) throws IOException {
        Map<String, double[]> algoMetrics = new HashMap<>();

        for (String algo : algoList) {
            String resDir = algo + File.separator + "given_" + sparsityLevel + File.separator + "top_" + reclistSize;
            algoMetrics.put(algo, getAveragedMetricForUser(resDir, testDir, numSplit, totUsers, reclistSize));
        }


        return algoMetrics;
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                Properties prop = new Properties();
                prop.load(new FileReader(args[0]));

                // path to a txt file where are listed all the algorithms
                String algoListFileName = prop.getProperty("algoListFileName"),
                        resDir = prop.getProperty("resDir"),  // where you want to put the results
                        testDir = prop.getProperty("testDir"); // directory in which there are the test file in trec_Eval format


                int reclistSize = Integer.parseInt(prop.getProperty("reclistSize")), // e.g., recommendation list size
                        totUsers = Integer.parseInt(prop.getProperty("totUsers")), // number of users in the dataset
                        numSplit = Integer.parseInt(prop.getProperty("numSplit"));
                String[] sparsityLevels = prop.getProperty("sparsityLevels").split(",");
                List<String> algorithmPaths = readAlgorithmsPath(algoListFileName);

                Map<String, Map<String, double[]>> algoMetricsForSparsity = new HashMap<>();

                for (String sparsityLevel : sparsityLevels) {
                    algoMetricsForSparsity.put(sparsityLevel, computeAllAlgoMetrics(algorithmPaths, sparsityLevel, testDir, totUsers, numSplit, reclistSize));
                }

                for (String sparsityLevel : sparsityLevels) {
                    String resFile = resDir + File.separator + "sign_given_" + sparsityLevel + ".csv";
                    BufferedWriter writer = null;
                    Map<String, double[]> currAlgoMetrics = algoMetricsForSparsity.get(sparsityLevel);
                    try {
                        writer = new BufferedWriter(new FileWriter(resFile));
                        writer.append("Sys1\tSys2\tT-test");
                        writer.newLine();

                        for (String sys1 : algorithmPaths) {
                            for (String sys2 : algorithmPaths) {
                                if (!sys1.equals(sys2)) {
                                    Logger.getLogger(SignificanceTest.class.getName()).log(Level.INFO, "Compare {0} VS {1}", new Object[]{sys1, sys2});

                                    writer.append(sys1);
                                    writer.append("\t").append(sys2);


                                    double[] sample1 = currAlgoMetrics.get(sys1),
                                            sample2 = currAlgoMetrics.get(sys2);

                                    writer.append("\t");
                                    writer.append(String.valueOf(TestUtils.pairedTTest(sample1, sample2)));
                                }

                            }
                        }


                    } finally {
                        if (writer != null)
                            writer.close();
                    }
                }

            } catch (Exception ex) {
                Logger.getLogger(SignificanceTest.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.err.println("Number of arguments not valid.");
            System.out.println();
            System.out.println("Usage: <properties_file>");
        }

    }
}


