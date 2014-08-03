package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.graph.indexer.LODIndexer;
import di.uniba.it.lodrecsys.graph.scorer.JaccardVertexTransformer;
import di.uniba.it.lodrecsys.properties.PropertiesCalculator;
import di.uniba.it.lodrecsys.properties.Similarity;
import di.uniba.it.lodrecsys.properties.SimilarityFunction;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by asuglia on 8/3/14.
 */
public class UserItemCosine extends RecGraph {

    private Map<String, String> idUriMap;
    private Map<String, String> uriIdMap;
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;
    private PropertiesManager propManager;
    private Map<String, Map<String, Double>> simUserMap;
    private LODIndexer indexer;

    public UserItemCosine(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) {
        try {
            getMapForMappedItems(mappedItems);
            generateGraph(new RequestStruct(trainingFileName, testFile, proprIndexDir, mappedItems));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getMapForMappedItems(List<MovieMapping> movieList) {
        // key: item-id - value: dbpedia uri
        idUriMap = new HashMap<>();
        uriIdMap = new HashMap<>();

        for (MovieMapping movie : movieList) {
            idUriMap.put(movie.getItemID(), movie.getDbpediaURI());
            uriIdMap.put(movie.getDbpediaURI(), movie.getItemID());
        }
    }

    @Override
    public void generateGraph(RequestStruct requestStruct) throws IOException {
        String trainingFileName = (String) requestStruct.params.get(0),
                testFile = (String) requestStruct.params.get(1);

        propManager = new PropertiesManager((String) requestStruct.params.get(2));
        List<MovieMapping> mappedItemsList = (List<MovieMapping>) requestStruct.params.get(3);
        getMapForMappedItems(mappedItemsList);


        trainingPosNeg = Utils.loadPosNegRatingForEachUser(trainingFileName);
        testSet = Utils.loadRatedItems(new File(testFile), false);

        Set<String> allItemsID = new TreeSet<>();

        for (Set<String> items : testSet.values()) {
            allItemsID.addAll(items);
        }

        for (String userID : trainingPosNeg.keySet()) {
            allItemsID.addAll(trainingPosNeg.get(userID).get(0));

        }

        indexer = new LODIndexer(propManager, trainingPosNeg, idUriMap);
        //computeSimilarityMap(calculator.getSimilarity(), allItemsID);
        Double meanSimUser = computeMeanUserSimilarity();

        for (String itemID : allItemsID) {
            recGraph.addVertex("I:" + itemID);
        }

        Set<String> userSet = trainingPosNeg.keySet();
        for (String userID : userSet) {
            int edgeCounter = 0;

            for (String posItemID : trainingPosNeg.get(userID).get(0)) {
                recGraph.addEdge("U:" + userID + "-" + edgeCounter, "U:" + userID, "I:" + posItemID);
                edgeCounter++;

            }

            Map<String, Double> currUserSimMap = simUserMap.get(userID);
            if (currUserSimMap != null) {

                for (String otherUser : userSet) {

                    if (currUserSimMap.get("U:" + otherUser) >= meanSimUser) {
                        String currUserGraph = "U:" + userID, otherUserGraph = "U:" + otherUser;

                        if (recGraph.findEdge(currUserGraph, otherUserGraph) == null)
                            recGraph.addEdge(currUserGraph + "-" + otherUserGraph, currUserGraph, otherUserGraph);
                    }

                }
            }
        }

        currLogger.info(String.format("Total number of vertex %s - Total number of edges %s", recGraph.getVertexCount(), recGraph.getEdgeCount()));

    }

    private Double computeMeanUserSimilarity() {
        Double totalSum = 0d;
        int numAdd = 0;

        for (String currUser : simUserMap.keySet()) {
            Map<String, Double> currUserSim = simUserMap.get(currUser);

            for (String entityID : currUserSim.keySet()) {
                if (entityID.startsWith("U:")) {
                    totalSum += currUserSim.get(entityID);
                    numAdd++;
                }
            }
        }

        return totalSum / numAdd;
    }

    private void computeSimilarityMap(SimilarityFunction function, Set<String> allItems) {
        this.simUserMap = new HashMap<>();

        Set<String> userSet = trainingPosNeg.keySet();
        double sumSimilarity = 0, minSimilarity = Double.MAX_VALUE;

        //TODO: NOT IMPLEMENTED YET


        // set missed minimum similarity value for not mapped item
        replaceNullFields(minSimilarity);

        // normalize matrix
        normalizeSimilarityScore(sumSimilarity);


    }

    private void replaceNullFields(Double minValue) {
        for (String currUser : this.simUserMap.keySet()) {
            Map<String, Double> currUserSim = simUserMap.get(currUser);
            for (String entityID : currUserSim.keySet()) {
                if (currUserSim.get(entityID) == null)
                    currUserSim.put(entityID, minValue);
            }
        }
    }

    private void normalizeSimilarityScore(Double sumValue) {
        for (String currUser : simUserMap.keySet()) {
            Map<String, Double> currUserSim = simUserMap.get(currUser);

            for (String entityID : currUserSim.keySet()) {
                currUserSim.put(entityID, currUserSim.get(entityID) / sumValue);
            }
        }
    }

    @Override
    public Map<String, Set<Rating>> runPageRank(RequestStruct requestParam) throws IOException {
        Map<String, Set<Rating>> usersRecommendation = new HashMap<>();

        double massProb = (double) requestParam.params.get(0); // max proportion of positive items for user

        // compute recommendation for all users

        for (String userID : testSet.keySet()) {
            currLogger.info("Page rank for user: " + userID);
            List<Set<String>> posNegativeRatings = trainingPosNeg.get(userID);
            Set<String> testItems = testSet.get(userID);
            usersRecommendation.put(userID, profileUser(userID, posNegativeRatings.get(0), posNegativeRatings.get(1), testItems));
        }

        return usersRecommendation;
    }

    private Map<String, Multimap<String, String>> loadItemsRepresentation(Collection<String> allItems, PropertiesManager manager) {
        Map<String, Multimap<String, String>> itemsRepresentation = new HashMap<>();

        for (String itemID : allItems) {
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

        return itemsRepresentation;

    }

    private Set<Rating> profileUser(String userID, Set<String> trainingPos, Set<String> trainingNeg, Set<String> testItems) {
        Set<Rating> allRecommendation = new TreeSet<>();

        JaccardVertexTransformer transformer = new JaccardVertexTransformer(userID, trainingPos, trainingNeg, simUserMap);
        PageRankWithPriors<String, String> priors = new PageRankWithPriors<>(this.recGraph, transformer, 0.15);

        priors.setMaxIterations(25);
        priors.evaluate();

        for (String currItemID : testItems) {
            allRecommendation.add(new Rating(currItemID, String.valueOf(priors.getVertexScore("I:" + currItemID))));

        }

        return allRecommendation;
    }
}
