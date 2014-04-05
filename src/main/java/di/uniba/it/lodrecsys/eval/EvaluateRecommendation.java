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
    private Set<Long> testSet;
    private int numberRecommendation;
    private Logger logger = Logger.getLogger(EvaluateRecommendation.class.getName());

    public EvaluateRecommendation(Recommender recommender, File testSetFile, NumRec numRec) throws TasteException, IOException {
        this.recommender = recommender;
        this.numberRecommendation = numRec.getValue();
        readTestSet(testSetFile);
    }

    private void readTestSet(File testSetFile) throws IOException {
        CSVParser parser = new CSVParser(new FileReader(testSetFile), CSVFormat.newFormat(' '));
        this.testSet = new TreeSet<>();

        for (CSVRecord record : parser.getRecords()) {
            this.testSet.add(Long.valueOf(record.get(0)));
        }
    }

    /**
     * Trec eval results format
     * <id_user> Q0 <id_item> <posizione nel rank> <score> <nome esperimento>
     */
    public void generateTrecEvalFile(String trecFilename) throws IOException, TasteException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(trecFilename));

            logger.info("Number of user in testset: " + this.testSet.size());
            for (Long userID : this.testSet) {
                List<RecommendedItem> recommendedItemList = recommender.recommend(userID, numberRecommendation);
                for (int i = 0; i < recommendedItemList.size(); i++) {
                    long itemID = recommendedItemList.get(i).getItemID();
                    String line = userID + " Q0 " + itemID + " " + (i + 1) + " " + recommender.estimatePreference(userID, itemID) + " " +
                            recommender.getClass().getSimpleName() + "-" + numberRecommendation;
                    writer.write(line);
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

}
