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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by asuglia on 4/4/14.
 */
public class EvaluateRecommendation {
    private static class UserItem {
        private String idUser;
        private String idItem;

        private UserItem(String idUser, String idItem) {
            this.idUser = idUser;
            this.idItem = idItem;
        }

        public String getIdUser() {
            return idUser;
        }

        public void setIdUser(String idUser) {
            this.idUser = idUser;
        }

        public String getIdItem() {
            return idItem;
        }

        public void setIdItem(String idItem) {
            this.idItem = idItem;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserItem userItem = (UserItem) o;

            if (idItem != null ? !idItem.equals(userItem.idItem) : userItem.idItem != null) return false;
            if (idUser != null ? !idUser.equals(userItem.idUser) : userItem.idUser != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = idUser != null ? idUser.hashCode() : 0;
            result = 31 * result + (idItem != null ? idItem.hashCode() : 0);
            return result;
        }
    }

    private Recommender recommender;
    private Map<UserItem, String> testSet;
    private int numberRecommendation;
    private Logger logger = Logger.getLogger(EvaluateRecommendation.class.getName());

    public EvaluateRecommendation(Recommender recommender, File testSetFile, NumRec numRec) throws TasteException, IOException {
        this.recommender = recommender;
        this.numberRecommendation = numRec.getValue();
        readTestSet(testSetFile);
    }

    private void readTestSet(File testSetFile) throws IOException {
        CSVParser parser = new CSVParser(new FileReader(testSetFile), CSVFormat.newFormat(' '));
        this.testSet = new HashMap<>();

        for (CSVRecord record : parser.getRecords()) {
            testSet.put(new UserItem(record.get(0), record.get(1)), record.get(2));
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


            UserItem currPair = new UserItem("", "");

            for (UserItem u : testSet.keySet()) {
                Long userID = Long.parseLong(u.getIdUser());
                List<RecommendedItem> recommendedItemList = recommender.recommend(userID, numberRecommendation);
                for (int i = 0; i < recommendedItemList.size(); i++) {
                    long itemID = recommendedItemList.get(i).getItemID();
                    currPair.setIdUser(userID + "");
                    currPair.setIdItem(itemID + "");

                    String line = userID + " Q0 " + itemID + " " + (i + 1) + " " + "1" + " " +
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
