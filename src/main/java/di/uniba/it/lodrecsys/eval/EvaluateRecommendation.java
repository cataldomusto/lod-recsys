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

    /**
     * Serializes a specific number of recommendation for each user according to
     * the TREC evaluation file format
     *
     * @param recommendationList all the recommendation for each user
     * @param resFile            the result's filename
     * @param numRec             number of recommendation that will be saved (-1 if all of them needed)
     * @throws IOException unable to write the result file
     */
    public static void serializeRatings(Map<String, Set<Rating>> recommendationList, String resFile, int numRec) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(resFile));


            for (String userID : recommendationList.keySet()) {
                Set<Rating> recommendationListForUser = recommendationList.get(userID);
                int i = 0;
                for (Rating rate : recommendationListForUser) {
                    String trecLine = userID + " Q0 " + rate.getItemID() + " " + i++ + " " + rate.getRating() + " EXP";
                    writer.write(trecLine);
                    writer.newLine();

                    // prints only numRec recommendation on file
                    if (numRec != -1 && i == numRec)
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
     * Transforms the MyMediaLite prediction's file into
     * a TREC eval results file format.
     * <p/>
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

    /**
     * Transforms an array of strings which contains prediction in the format
     * item_id:rating, into a set of Rating
     *
     * @param ratings array of rating in string form
     * @return an ordered list of ratings
     */
    private static Set<Rating> getRatingsSet(String[] ratings) {
        Set<Rating> ratingSet = new TreeSet<>();

        for (String rating : ratings) {
            String splitted[] = rating.split(":");
            ratingSet.add(new Rating(splitted[0], splitted[1]));
        }

        return ratingSet;

    }

    /**
     * Executes the trec_eval tool to evaluate the produced results
     * computing per user metrics and saves all in a file
     *
     * @param goldStandardFile filename of the test file in trec_eval format
     * @param resultFile       filename of the results file in trec_eval format
     * @param trecResultFile   filename of the results produced by trec_eval
     */
    public static void savePerUserTrec(String goldStandardFile, String resultFile, String trecResultFile) {
        String trecEvalCommand = "trec_eval -q -m all_trec " + goldStandardFile + " " + resultFile;

        CmdExecutor.executeCommandAndPrintLinux(trecEvalCommand, trecResultFile);
        logger.info(trecEvalCommand);
    }

    /**
     * Executes the trec_eval tool to evaluate the produced results
     * and saves them in a file
     *
     * @param goldStandardFile filename of the test file in trec_eval format
     * @param resultFile       filename of the results file in trec_eval format
     * @param trecResultFile   filename of the results produced by trec_eval
     */
    public static void saveTrecEvalResult(String goldStandardFile, String resultFile, String trecResultFile) {
        String trecEvalCommand = "trec_eval -m all_trec " + goldStandardFile + " " + resultFile;

        CmdExecutor.executeCommandAndPrint(trecEvalCommand, trecResultFile);
        logger.info(trecEvalCommand);
    }

    /**
     * Parses the specified trec eval results file and retrives all the produced
     * metrics
     *
     * @param trecEvalFile filename of the trec_eval results file
     * @return a dictionary whose keys are metrics' name and whose values are metrics' values
     * @throws IOException
     */
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

    /**
     * Computes F1-measure from the precision and recall specified
     * values
     *
     * @param precision current precision
     * @param recall    current recall
     * @return f1-measure
     */
    public static float getF1(float precision, float recall) {
        return (precision == 0 && precision == recall) ? 0 : (2 * precision * recall) / (precision + recall);

    }

    /**
     * Computes F1-measure for all the cut-off levels defined
     * which are: 5, 10, 15, 20
     *
     * @param measures the metrics' map that will be updated with f1-measures
     */
    private static void evalF1Measure(Map<String, Float> measures) {
        int[] cutoffLevels = new int[]{5, 10, 15, 20};
        String precisionString = "P", recallString = "recall", fMeasureString = "F1";

        for (int cutoff : cutoffLevels) {
            String currPrecision = precisionString + "_" + cutoff,
                    currRecall = recallString + "_" + cutoff;

            measures.put(fMeasureString + "_" + cutoff, getF1(measures.get(currPrecision), measures.get(currRecall)));
        }


    }

    /**
     * Averages the metrics results for each split
     *
     * @param metricsValuesForSplit list of metrics computed for each split
     * @param numberOfSplit         number of split
     * @return string representation of the results
     */
    public static String averageMetricsResult(List<Map<String, String>> metricsValuesForSplit, int numberOfSplit) {
        StringBuilder results = new StringBuilder("");
        String[] usefulMetrics = {"P_5", "P_10", "P_15", "P_20", "recall_5", "recall_10",
                "recall_15", "recall_20"},
                completeMetrics = {"P_5", "P_10", "P_15", "P_20", "recall_5", "recall_10",
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

    /**
     * Serializes the metrics results coming from the evaluation process in a file
     *
     * @param metricsResult      string representation of the results
     * @param completeReportFile filename of the metrics results
     * @throws IOException if unable to write the file
     */
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
