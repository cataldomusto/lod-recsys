package di.uniba.it.lodrecsys.eval;

import di.uniba.it.lodrecsys.utils.TrecEvalParser;
import org.apache.commons.math3.stat.inference.TestUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by asuglia on 5/17/14.
 */
public class SignificanceTest {
    private static Logger currLogger = Logger.getLogger(SignificanceTest.class.getName());

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

    private static double[] getAveragedMetricForUser(String resDir, String testDir, int numSplit, int reclistSize) throws IOException {

        Map<String, Float> userMetrics = new HashMap<>();
        String cutoff = String.valueOf(reclistSize), refMetric = "F1_" + cutoff;

        for (int i = 1; i <= numSplit; i++) {
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
                userMetrics.put(currUser, userMetrics.getOrDefault(currUser, 0f) + currMetrics.get(refMetric));
            }
        }

        double[] avgVector = new double[userMetrics.keySet().size()];
        for (String currUser : userMetrics.keySet()) {
            int userIndex = Integer.parseInt(currUser);
            avgVector[userIndex - 1] = userMetrics.get(currUser) / numSplit;
        }

        return avgVector;

    }

    private static Map<String, double[]> computeAllAlgoMetrics(List<String> algoList, String sparsityLevel, String testDir, int numSplit, int reclistSize) throws IOException {
        Map<String, double[]> algoMetrics = new HashMap<>();

        for (String algo : algoList) {
            String resDir = algo + File.separator + "given_" + sparsityLevel + File.separator + "top_" + reclistSize;
            algoMetrics.put(algo, getAveragedMetricForUser(resDir, testDir, numSplit, reclistSize));
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
                        numSplit = Integer.parseInt(prop.getProperty("numSplit"));
                String[] sparsityLevels = prop.getProperty("sparsityLevels").split(",");
                List<String> algorithmPaths = readAlgorithmsPath(algoListFileName);

                Map<String, Map<String, double[]>> algoMetricsForSparsity = new HashMap<>();

                for (String sparsityLevel : sparsityLevels) {
                    currLogger.info("Computing metrics for sparsity level given_" + sparsityLevel);
                    algoMetricsForSparsity.put(sparsityLevel, computeAllAlgoMetrics(algorithmPaths, sparsityLevel, testDir, numSplit, reclistSize));
                }

                for (String sparsityLevel : sparsityLevels) {
                    currLogger.info("Computing significance test for sparsity level given_" + sparsityLevel);
                    String resFile = resDir + File.separator + "sign_given_" + sparsityLevel + ".csv";
                    BufferedWriter writer = null;
                    Map<String, double[]> currAlgoMetrics = algoMetricsForSparsity.get(sparsityLevel);
                    try {
                        writer = new BufferedWriter(new FileWriter(resFile));
                        writer.append("Sys1\tSys2\tT-test");
                        writer.newLine();

                        Pattern algoNamePattern = Pattern.compile(".*" + File.separator + "results" + File.separator + "(.*)");

                        for (String sys1 : algorithmPaths) {
                            Matcher sys1Match = algoNamePattern.matcher(sys1);
                            String sys1Name = sys1;
                            if (sys1Match.matches()) {
                                sys1Name = sys1Match.group(1);
                            }

                            for (String sys2 : algorithmPaths) {
                                if (!sys1.equals(sys2)) {
                                    Matcher sys2Match = algoNamePattern.matcher(sys2);
                                    String sys2Name = sys2;
                                    if (sys2Match.matches()) {
                                        sys2Name = sys2Match.group(1);
                                    }
                                    currLogger.info(String.format("%s VS %s", sys1Name, sys2Name));

                                    writer.append(sys1Name);
                                    writer.append("\t").append(sys2Name);


                                    double[] sample1 = currAlgoMetrics.get(sys1),
                                            sample2 = currAlgoMetrics.get(sys2);

                                    writer.append("\t");
                                    writer.append(String.valueOf(TestUtils.pairedTTest(sample1, sample2)));
                                    writer.newLine();
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


