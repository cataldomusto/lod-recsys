package di.uniba.it.lodrecsys.eval;

import di.uniba.it.lodrecsys.utils.TrecEvalParser;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public static void main(String[] args) {
        if (args.length == 6) {
            try {

                String goldFile = args[0];
                File testDir = new File(args[1]);
                int n = Integer.parseInt(args[2]);
                double relScoreThreshold = Double.parseDouble(args[3]);
                Logger.getLogger(SignificanceTest.class.getName()).log(Level.INFO, "Gold standard {0}", goldFile);
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[4]));
                String refMetric = args[5] + "_" + n;
                writer.append("Sys1\tSys2\tT-test\tOne-way Anova\tWilcoxon Signed Rank Test\tMann Whitney U-test");
                writer.newLine();

                File[] files = testDir.listFiles();
                if (files != null)
                    for (int i = 0; i < files.length; i++) {
                        for (int j = 0; j < files.length; j++) {
                            if (i != j) {
                                File testFile1 = files[i];
                                File testFile2 = files[j];
                                Logger.getLogger(SignificanceTest.class.getName()).log(Level.INFO, "Compare {0} VS {1}", new Object[]{testFile1.getName(), testFile2.getName()});

                                writer.append(testFile1.getName());
                                writer.append("\t").append(testFile2.getName());


                                double[] sample1 = generateMeasureArray(TrecEvalParser.getPerUserMetrics(testFile1.getName()), refMetric),
                                        sample2 = generateMeasureArray(TrecEvalParser.getPerUserMetrics(testFile2.getName()), refMetric);

                                writer.append("\t");
                                writer.append(String.valueOf(TestUtils.pairedTTest(sample1, sample2)));
                                List classes = new ArrayList();
                                classes.add(sample1);
                                classes.add(sample2);
                                writer.append("\t");
                                writer.append(String.valueOf(TestUtils.oneWayAnovaPValue(classes)));
                                WilcoxonSignedRankTest wst = new WilcoxonSignedRankTest();
                                writer.append("\t");
                                writer.append(String.valueOf(wst.wilcoxonSignedRankTest(sample1, sample2, false)));
                                MannWhitneyUTest mwt = new MannWhitneyUTest();
                                writer.append("\t");
                                writer.append(String.valueOf(mwt.mannWhitneyUTest(sample1, sample2)));
                                writer.newLine();
                            }
                        }
                    }
                writer.close();
            } catch (Exception ex) {
                Logger.getLogger(SignificanceTest.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.err.println("Number of arguments not valid.");
            System.out.println();
            System.out.println("Usage: <gold standard> <testDir> <top-N> <relevanceScoreThreshold> <out filename> <ref_metric>");
        }

    }
}


