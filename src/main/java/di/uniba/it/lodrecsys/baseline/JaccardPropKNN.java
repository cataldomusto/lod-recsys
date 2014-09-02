package di.uniba.it.lodrecsys.baseline;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.properties.JaccardSimilarityFunction;
import di.uniba.it.lodrecsys.properties.SimilarityFunction;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by asuglia on 9/2/14.
 */
public class JaccardPropKNN {
    private static Logger currLogger = Logger.getLogger(JaccardPropKNN.class.getName());
    private PropertiesManager manager;
    private Map<String, Multimap<String, String>> itemsRepresentation;
    private Map<String, Set<SimScore>> itemSimScore;
    private SimilarityFunction simFunction;
    private Map<String, String> idUriMap;


    public static class SimScore implements Comparable<SimScore> {
        private String itemID;
        private Double similarity;

        public SimScore(String itemID, Double similarity) {
            this.itemID = itemID;
            this.similarity = similarity;
        }

        public String getItemID() {
            return itemID;
        }

        public void setItemID(String itemID) {
            this.itemID = itemID;
        }

        public Double getSimilarity() {
            return similarity;
        }

        public void setSimilarity(Double similarity) {
            this.similarity = similarity;
        }

        @Override
        public int compareTo(SimScore o) {
            if (this.similarity == null && o.similarity == null)
                return 0;
            if (this.similarity == null && o.similarity != null)
                return -1;

            if (this.similarity != null && o.similarity == null)
                return 1;

            return this.similarity != null ? this.similarity.compareTo(o.similarity) : -1;
        }

        @Override
        public String toString() {
            return itemID;
        }
    }

    public JaccardPropKNN(String trainFile, String propIndexDir, List<MovieMapping> mappedItems) throws IOException {
        manager = new PropertiesManager(propIndexDir);
        simFunction = new JaccardSimilarityFunction();
        itemSimScore = new HashMap<>();
        getMapForMappedItems(mappedItems, Utils.loadRatingForEachItem(trainFile).keySet());
        loadItemsRepresentation(manager);
    }

    private void getMapForMappedItems(List<MovieMapping> movieList, Set<String> allItems) {
        // key: item-id - value: dbpedia uri
        idUriMap = new HashMap<>();

        for (String itemID : allItems)
            idUriMap.put(itemID, null);

        for (MovieMapping movie : movieList) {
            idUriMap.put(movie.getItemID(), movie.getDbpediaURI());
        }
    }

    private void computeItemSimilarites() {
        double minSimilarity = Double.MAX_VALUE;
        Set<String> allItems = idUriMap.keySet();
        for (String currItemID : allItems) {
            currLogger.info("Computing similarities for item " + currItemID);
            Set<SimScore> currSimSet = new TreeSet<>();
            Multimap<String, String> currItemVector = itemsRepresentation.get(currItemID);


            for (String otherItemID : allItems) {
                Multimap<String, String> otherItemVector = itemsRepresentation.get(otherItemID);
                Double finalScore = null;

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
            replaceNullFields(currItemID, minSimilarity);
            minSimilarity = Double.MAX_VALUE;

        }


    }

    private void replaceNullFields(String currUser, Double minValue) {
        Set<SimScore> currItemSim = itemSimScore.get(currUser);
        for (SimScore score : currItemSim) {
            if (score.getSimilarity() == null) {
                score.setSimilarity(minValue);

            }
        }

    }

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

    private long getMaxID() {
        long maxID = Long.MIN_VALUE;

        for (String itemID : itemSimScore.keySet()) {
            long other = Long.parseLong(itemID);
            if (maxID < other)
                maxID = other;

        }

        return maxID;
    }

    public void serializeModelFormat(String modelFileName) throws IOException {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("version", "3.03");
        valuesMap.put("class_name", "MyMediaLite.ItemRecommendation.ItemKNN");
        valuesMap.put("correlation", "Cosine");
        String maxVal = String.valueOf(itemSimScore.keySet().size());
        valuesMap.put("k", maxVal);

        StringBuilder builder = new StringBuilder();

        for (String itemID : itemSimScore.keySet()) {
            builder.append(Joiner.on(" ").join(itemSimScore.get(itemID))).append("\n");
        }

        valuesMap.put("neighbours", builder.toString());

        builder = new StringBuilder();

        for (String itemID : itemSimScore.keySet()) {
            for (SimScore score : itemSimScore.get(itemID)) {
                builder.append(itemID).append(" ").append(score.getItemID()).append(" ").append(score.getSimilarity()).append("\n");

            }
        }

        valuesMap.put("similarities", builder.toString());

        String templateString = "${class_name}\n${version}\n${correlation}\n${k}\n${neighbours}${k}\n${similarities}";
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedString = sub.replace(templateString);

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(modelFileName));
            writer.write(resolvedString);
        } finally {
            if (writer != null)
                writer.close();
        }

    }

    public static void main(String[] args) throws IOException {
        String dataPath = "/home/asuglia/thesis/dataset/ml-100k/binarized",
                propertyIndexDir = "/home/asuglia/thesis/content_lodrecsys/movielens/stored_prop",
                mappedItemFile = "mapping/item.mapping";
        List<MovieMapping> mappingList = Utils.loadDBpediaMappedItems(mappedItemFile);

        JaccardPropKNN jaccard = new JaccardPropKNN(dataPath + File.separator + "u1.base", propertyIndexDir, mappingList);

        jaccard.computeItemSimilarites();

        jaccard.serializeModelFormat("/home/asuglia/model.txt");

    }


}

