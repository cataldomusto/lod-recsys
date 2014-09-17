package di.uniba.it.lodrecsys.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Utility class used to parse the trec eval results file
 * and to get a map which contains all the desired metrics
 */
public class TrecEvalParser {
    private static final String[] usefulMetrics = new String[]{"P_5", "P_10", "P_15",
            "P_20", "recall_5", "recall_10", "recall_15", "recall_20"};
    private static final List<String> listStrings = new ArrayList<>();

    static {
        Collections.addAll(listStrings, usefulMetrics);
    }

    private static boolean isPresentMetrics(String metric) {
        return listStrings.contains(metric);
    }

    public static Map<String, Map<String, Float>> getPerUserMetrics(String trecFinalName) throws IOException {
        Map<String, Map<String, Float>> perUserMetrics = new HashMap<>();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(trecFinalName));
            String currUser = "";
            Map<String, Float> currUserMetrics = new HashMap<>();
            while (reader.ready()) {
                String currMetricLine = reader.readLine();
                String[] splittedLine = currMetricLine.split("\t");
                String currMetric = splittedLine[0].trim();
                if (!isPresentMetrics(currMetric))
                    continue; // not relevant metrics

                if (currUser.equals("")) {
                    currUser = splittedLine[1];

                } else if (currUser.equals("all")) {
                    perUserMetrics.put(currUser, currUserMetrics);
                    break; // last user completed

                } else if (!currUser.equals(splittedLine[1])) {
                    perUserMetrics.put(currUser, currUserMetrics);
                    currUser = splittedLine[1];
                    currUserMetrics = new HashMap<>();
                }

                currUserMetrics.put(currMetric, Float.parseFloat(splittedLine[2]));

            }
        } catch (IOException e) {
            throw e;

        } finally {
            assert reader != null;
            reader.close();
        }

        return perUserMetrics;
    }

}
