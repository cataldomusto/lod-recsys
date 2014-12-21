package di.uniba.it.lodrecsys.baseline;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.properties.JaccardSimilarityFunction;
import di.uniba.it.lodrecsys.properties.SimilarityFunction;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by asuglia on 9/2/14.
 * <p/>
 * Class which defines the method in order to compute
 * similarities among items using the Linked Open Data
 * properties saved in your local TDB repository.
 */
public class JaccardPropKNN {
    private static Logger currLogger = Logger.getLogger(JaccardPropKNN.class.getName());
    private PropertiesManager manager;
    private Map<String, Multimap<String, String>> itemsRepresentation;
    private Map<String, Set<SimScore>> itemSimScore;
    private SimilarityFunction simFunction;
    private Map<String, String> idUriMap;


    /**
     * Constructs this object to compute similarities among
     * the items in the specified training set
     *
     * @param trainFile    the current training set
     * @param propIndexDir Jena TDB stored index
     * @param mappedItems  All the items that are effectively mapped with the LODC
     * @throws IOException If one of the needed file is incorrect or doesn't exist
     */
    public JaccardPropKNN(String trainFile, String propIndexDir, List<MovieMapping> mappedItems) throws IOException {
        manager = new PropertiesManager(propIndexDir);
        simFunction = new JaccardSimilarityFunction();
        itemSimScore = new HashMap<>();
        getMapForMappedItems(mappedItems, Utils.loadRatingForEachItem(trainFile).keySet());
        loadItemsRepresentation(manager);
    }

    public static void main(String[] args) throws IOException {
        /*
            EXAMPLE VALUES:

        String trainPath = "/home/asuglia/thesis/dataset/ml-100k/definitive",
                simPath = "/home/asuglia/thesis/dataset/ml-100k/results/ItemKNNLod/similarities",
                propertyIndexDir = "/home/asuglia/thesis/content_lodrecsys/movielens/stored_prop",
                mappedItemFile = "mapping/item.mapping";
        */
        Properties prop = new Properties();
        prop.load(new FileReader(args[0]));
        String trainPath = prop.getProperty("trainPath"),
                simPath = prop.getProperty("simPath"),
                propertyIndexDir = prop.getProperty("propertyIndexDir"),
                mappedItemFile = prop.getProperty("mappedItemFile");

        List<MovieMapping> mappingList = Utils.loadDBpediaMappedItems(mappedItemFile);

        for (SparsityLevel level : SparsityLevel.values()) {
            for (int i = 1; i <= 5; i++) {
                JaccardPropKNN jaccard = new JaccardPropKNN(trainPath + File.separator + level
                        + File.separator + "u" + i + ".base", propertyIndexDir, mappingList);

                jaccard.computeItemSimilarites();

                jaccard.serializeSimMatrix(simPath + File.separator + level + File.separator + "u" + i + ".sim");

            }

        }

    }

    /**
     * Obtains an easy to use map that is able to convert a specific
     * item-id defined in the dataset to a HTTP URI that represents the entity
     * in the LODC
     *
     * @param movieList list of mapped items
     * @param allItems  list of dataset items' id
     */
    private void getMapForMappedItems(List<MovieMapping> movieList, Set<String> allItems) {
        // key: item-id - value: dbpedia uri
        idUriMap = new HashMap<>();

        for (String itemID : allItems)
            idUriMap.put(itemID, null);

        for (MovieMapping movie : movieList) {
            idUriMap.put(movie.getItemID(), movie.getDbpediaURI());
        }
    }

    /**
     * Populates the similarity matrix computing the similarities among all the
     * items.
     */
    private void computeItemSimilarites() {
        float minSimilarity = simFunction.getMaxValue();
        Set<String> allItems = idUriMap.keySet();
        for (String currItemID : allItems) {
            currLogger.info("Computing similarities for item " + currItemID);
            Set<SimScore> currSimSet = new TreeSet<>();
            Multimap<String, String> currItemVector = itemsRepresentation.get(currItemID);


            for (String otherItemID : allItems) {
                Multimap<String, String> otherItemVector = itemsRepresentation.get(otherItemID);
                Float finalScore = null;

                if (currItemVector != null && otherItemVector != null) {
                    finalScore = simFunction.compute(currItemVector, otherItemVector);
                }

                currSimSet.add(new SimScore(otherItemID, finalScore));
                if (finalScore != null) {
                    // update minimum score similarity
                    if (finalScore < minSimilarity)
                        minSimilarity = finalScore;
                }

            }


            this.itemSimScore.put(currItemID, currSimSet);

            // set missed minimum similarity value for not mapped item
            if (minSimilarity == simFunction.getMaxValue()) // all one or all null for the current user
                minSimilarity = simFunction.getMinValue();

            replaceNullFields(currItemID, minSimilarity);
            minSimilarity = simFunction.getMaxValue();

        }


    }

    /**
     * Replaces all the null values in the current item similarity list
     * with a specified value
     *
     * @param currItem the item for which will be done the substitution
     * @param minValue the value that will replace the "null"
     */
    private void replaceNullFields(String currItem, Float minValue) {
        Set<SimScore> currItemSim = itemSimScore.get(currItem);
        for (SimScore score : currItemSim) {
            if (score.getSimilarity() == null) {
                score.setSimilarity(minValue);

            }
        }

    }

    /**
     * Uses the specified properties manager in order to construct a
     * LOD representation for each mapped item.
     * For each property could be multiple values associated to the same
     * item.
     *
     * @param manager object that manages the Jena TDB stored index
     */
    private void loadItemsRepresentation(PropertiesManager manager) {
        itemsRepresentation = new HashMap<>();

        for (String itemID : idUriMap.keySet()) {
            String resourceID = idUriMap.get(itemID);
            if (resourceID != null) {
                List<Statement> propList = manager.getResourceProperties(resourceID);
                Multimap<String, String> itemProperties = ArrayListMultimap.create();

                for (Statement stat : propList) {
                    itemProperties.put(stat.getPredicate().toString(), stat.getObject().toString());
                }

                itemsRepresentation.put(itemID, itemProperties);

            }
        }

    }

    /**
     * Returns the maximum item ID from the similarity matrix and
     * converts it in a long
     *
     * @return the maximum available id
     */
    private long getMaxID() {
        long maxID = Long.MIN_VALUE;

        for (String itemID : itemSimScore.keySet()) {
            long other = Long.parseLong(itemID);
            if (maxID < other)
                maxID = other;

        }

        return maxID;
    }

    /**
     * Prints in a formatted file the computed similarity matrix
     *
     * @param simMatrixFile The filename in which the matrix will be saved
     * @throws IOException If unable to open the file
     */
    public void serializeSimMatrix(String simMatrixFile) throws IOException {
        StringBuilder builder = new StringBuilder();

        for (String itemID : itemSimScore.keySet()) {
            for (SimScore score : itemSimScore.get(itemID)) {
                builder.append(itemID).append(" ").append(score.getItemID()).append(" ").append(score.getSimilarity()).append("\n");

            }
        }

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(simMatrixFile));
            writer.write(String.valueOf(getMaxID() + 1)); // number of items
            writer.newLine();
            writer.write(builder.toString()); // similarities
        } finally {
            if (writer != null)
                writer.close();
        }

    }

    /**
     * Class which defines the similarity score between two items
     */
    public static class SimScore implements Comparable<SimScore> {
        private String itemID;
        private Float similarity;

        public SimScore(String itemID, Float similarity) {
            this.itemID = itemID;
            this.similarity = similarity;
        }

        public String getItemID() {
            return itemID;
        }

        public void setItemID(String itemID) {
            this.itemID = itemID;
        }

        public Float getSimilarity() {
            return similarity;
        }

        public void setSimilarity(Float similarity) {
            this.similarity = similarity;
        }

        @Override
        public int compareTo(SimScore o) {
            if (this.similarity == null || o.similarity == null)
                return this.itemID.compareTo(o.itemID);

            return this.similarity != null ? this.similarity.compareTo(o.similarity) : -1;
        }

        @Override
        public String toString() {
            return itemID;
        }
    }


}

