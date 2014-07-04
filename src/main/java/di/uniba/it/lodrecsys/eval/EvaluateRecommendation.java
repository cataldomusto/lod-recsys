package di.uniba.it.lodrecsys.eval;

import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.utils.CmdExecutor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by asuglia on 4/4/14.
 */
public class EvaluateRecommendation {
    private static Logger logger = Logger.getLogger(EvaluateRecommendation.class.getName());

    public static void serializeRatings(Map<String, Set<Rating>> recommendationList, String resFile, int numRec) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(resFile));

            int i = 0;
            for (String userID : recommendationList.keySet()) {
                Set<Rating> recommendationListForUser = recommendationList.get(userID);

                for (Rating rate : recommendationListForUser) {
                    String trecLine = userID + " Q0 " + rate.getItemID() + " " + i++ + " " + rate.getRating() + " EXP";
                    writer.write(trecLine);
                    writer.newLine();

                    // prints only numRec recommendation on file
                    if (i++ == numRec)
                        break;
                }
            }
        } catch (IOException e) {
            logger.severe(e.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }


    }


    /**
     * Trec eval results format
     * <id_user> Q0 <id_item> <posizione nel rank> <score> <nome esperimento>
     */
    public static void generateTrecEvalFile(String resultFile, String outTrecFile) throws IOException {
        PrintWriter writer = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(resultFile));
            writer = new PrintWriter(new FileWriter(outTrecFile));

            String currUser = "";
            Set<Rating> currUserRatings = new TreeSet<>();

            while (reader.ready()) {
                String line = reader.readLine();
                String[] lineSplitted = line.split("\t");
                String userID = lineSplitted[0];

                String ratingString = lineSplitted[1].substring(lineSplitted[1].indexOf("[") + 1, lineSplitted[1].indexOf("]"));

                Set<Rating> ratings = getRatingsSet(ratingString.split(","));
                int i = 0;

                for (Rating rate : ratings) {

                    String trecLine = userID + " Q0 " + rate.getItemID() + " " + i++ + " " + rate.getRating() + " EXP";
                    writer.println(trecLine);
                }


            }


        } catch (IOException ex) {
            throw new IOException(ex);
        } finally {
            assert reader != null;
            reader.close();
            assert writer != null;
            writer.close();
        }


    }



    private static Set<Rating> getRatingsSet(String[] ratings) {
        Set<Rating> ratingSet = new TreeSet<>();

        for (String rating : ratings) {
            String splitted[] = rating.split(":");
            ratingSet.add(new Rating(splitted[0], splitted[1]));
        }

        return ratingSet;

    }

    public static void saveTrecEvalResult(String goldStandardFile, String resultFile, String trecResultFile) {
        String trecEvalCommand = "trec_eval -m all_trec " + goldStandardFile + " " + resultFile;

        CmdExecutor.executeCommandAndPrint(trecEvalCommand, trecResultFile);
        logger.info(trecEvalCommand);
    }

    public static Map<String, String> getTrecEvalResults(String trecEvalFile) throws IOException {
        CSVParser parser = null;
        Map<String, String> trecMetrics = new HashMap<>();

        try {
            parser = new CSVParser(new FileReader(trecEvalFile), CSVFormat.TDF);
            logger.info("Loading trec eval metrics from: " + trecEvalFile);
            for (CSVRecord rec : parser.getRecords()) {
                trecMetrics.put(rec.get(0), rec.get(2));
            }

            return trecMetrics;
        } catch (IOException e) {
            throw e;
        } finally {
            assert parser != null;
            parser.close();
        }

    }


    private static float getF1(float precision, float recall) {
        return (2 * precision * recall) / (precision + recall);

    }

    private static void evalF1Measure(Map<String, Float> measures) {
        int[] cutoffLevels = new int[]{5, 10, 15, 20};
        String precisionString = "P", recallString = "recall", fMeasureString = "F1";

        for (int cutoff : cutoffLevels) {
            String currPrecision = precisionString + "_" + cutoff,
                    currRecall = recallString + "_" + cutoff;

            measures.put(fMeasureString + "_" + cutoff, getF1(measures.get(currPrecision), measures.get(currRecall)));
        }


    }

    public static String averageMetricsResult(List<Map<String, String>> metricsValuesForSplit, int numberOfSplit) {
        StringBuilder results = new StringBuilder("");
        String[] usefulMetrics = {"map", "P_5", "P_10", "P_15", "P_20", "recall_5", "recall_10",
                "recall_15", "recall_20"},
                completeMetrics = {"map", "P_5", "P_10", "P_15", "P_20", "recall_5", "recall_10",
                        "recall_15", "recall_20", "F1_5", "F1_10", "F1_15", "F1_20"};

        Map<String, Float> averageRes = new HashMap<>();

        for (String measure : usefulMetrics) {
            float currMetricsTot = 0f;
            for (Map<String, String> map : metricsValuesForSplit) {
                currMetricsTot += Float.parseFloat(map.get(measure));
            }
            averageRes.put(measure, currMetricsTot / numberOfSplit);
        }

        evalF1Measure(averageRes);

        for (String measure : completeMetrics) {
            results.append(measure).append("=").append(averageRes.get(measure)).append("\n");
        }

        return results.toString();

    }

    public static void generateMetricsFile(String metricsResult, String completeReportFile) throws IOException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(completeReportFile));
            writer.write(metricsResult);

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            assert writer != null;
            writer.close();

        }


    }

}
