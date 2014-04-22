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
     *
     * Trec eval results format
     * <id_user> Q0 <id_item> <posizione nel rank> <score> <nome esperimento>
     */
    public static void generateTrecEvalFile(String resultFile, String outTrecFile) throws IOException {
        PrintWriter writer = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(resultFile));
            writer = new PrintWriter(new FileWriter(outTrecFile));

            while (reader.ready()) {
                String line = reader.readLine();
                String[] lineSplitted = line.split("\t");
                String userID = lineSplitted[0];
                Set<Rating> ratings = getRatingsSet(lineSplitted[1].split(","));
                int i = 0;
                for (Rating rate : ratings) {
                    String trecLine = userID + " Q0 " + rate.getItemID() + " " + i++ + " " + rate.getRating() + " EXP\n";
                    writer.write(trecLine);
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
            if (splitted[0].startsWith("[")) {
                splitted[0] = splitted[0].substring(1, splitted[0].length());
            }

            if (splitted[1].endsWith("]")) {
                splitted[1] = splitted[1].substring(0, splitted[1].length() - 1);
            }
            ratingSet.add(new Rating(splitted[0], splitted[1]));
        }

        return ratingSet;

    }

    public static void saveTrecEvalResult(String goldStandardFile, String resultFile, int numExperiment) {
        String trecEvalCommand = "trec_eval " + goldStandardFile + " " + resultFile,
                trecResultFile = resultFile.substring(0, resultFile.lastIndexOf(File.separator))
                        + File.separator + "u" + numExperiment + ".final";

        System.out.println(trecResultFile);

        CmdExecutor.executeCommandAndPrint(trecEvalCommand, trecResultFile);

    }

}
