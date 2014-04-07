package di.uniba.it.lodrecsys.eval;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveArrayIterator;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by asuglia on 4/4/14.
 */
public class EvaluateRecommendation {


    private Recommender recommender;
    private DataModel testSet;
    private int numberRecommendation;
    private Logger logger = Logger.getLogger(EvaluateRecommendation.class.getName());

    public EvaluateRecommendation(Recommender recommender, DataModel testSet, NumRec numRec) throws TasteException, IOException {
        this.recommender = recommender;
        this.numberRecommendation = numRec.getValue();
        this.testSet = testSet;

    }

    /**
     *
     * Trec eval results format
     * <id_user> Q0 <id_item> <posizione nel rank> <score> <nome esperimento>
     */
    public void generateTrecEvalFile(String trecFilename) throws IOException, TasteException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(trecFilename));

            for (LongPrimitiveIterator iterUser = testSet.getUserIDs(); iterUser.hasNext(); ) {
                int contRec = 0;
                long userID = iterUser.nextLong();
                for (LongPrimitiveIterator iterItem = testSet.getItemIDsFromUser(userID).iterator(); iterItem.hasNext(); ) {
                    long itemID = iterItem.nextLong();
                    String formattedLine = userID + " Q0 " + itemID + " " + contRec++ + " " +
                            recommender.estimatePreference(userID, itemID) + " " + recommender.getClass().getSimpleName() + "-" + this.numberRecommendation;
                    writer.write(formattedLine);
                    writer.newLine();

                }


            }

        } catch (IOException | TasteException ex) {
            logger.severe(ex.getMessage());
        } finally {
            assert (writer != null);
            writer.close();
        }
    }


    /**
     * Executes a specific command to the BASH and save the results printed on the
     * stdout into a file whose name is the one specified in input.
     *
     * @param command        the command that will be executed
     * @param resultFilename the file that will contain the output of the command
     */
    private static void executeCommand(String command, String resultFilename) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(resultFilename));
            writer.write(output.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert writer != null;
            writer.close();
        }
    }

    public void saveTrecEvalResult(String goldStandardFile, String resultFile, int numExperiment) {
        String trecEvalCommand = "trec_eval " + goldStandardFile + " " + resultFile,
                trecResultFile = goldStandardFile.substring(0, goldStandardFile.lastIndexOf(File.separator))
                        + File.separator + "u" + numExperiment + ".final";

        System.out.println(trecResultFile);

        executeCommand(trecEvalCommand, trecResultFile);

    }

}
