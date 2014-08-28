package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.graph.scorer.SimilarityVertexTransformer;
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
 * Created by asuglia on 7/30/14.
 */
public class UserItemJaccard extends RecGraph {
    private Map<String, String> idUriMap;
    private Map<String, String> uriIdMap;
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;
    private Map<String, Multimap<String, String>> usersCentroid;
    private PropertiesManager propManager;
    private Map<String, Multimap<String, String>> itemsRepresentation;
    private int totalNumberItems;
    private Map<String, Map<String, Double>> simUserMap;


    public UserItemJaccard(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) {
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
        usersCentroid = new HashMap<>();

        trainingPosNeg = Utils.loadPosNegRatingForEachUser(trainingFileName);
        testSet = Utils.loadRatedItems(new File(testFile), false);

        Set<String> allItemsID = new TreeSet<>();

        for (Set<String> items : testSet.values()) {
            allItemsID.addAll(items);
        }

        for (String userID : trainingPosNeg.keySet()) {
            allItemsID.addAll(trainingPosNeg.get(userID).get(0));

        }

        totalNumberItems = allItemsID.size();

        itemsRepresentation = loadItemsRepresentation(allItemsID, propManager);
        PropertiesCalculator calculator = PropertiesCalculator.create(Similarity.JACCARD);
        for (String userID : trainingPosNeg.keySet()) {
            usersCentroid.put(userID, calculator.computeCentroid(getRatedItemRepresentation(trainingPosNeg.get(userID).get(0))));
        }

        computeSimilarityMap(calculator.getSimilarity(), allItemsID);
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

        for (String currUser : userSet) {
            Multimap<String, String> currUserVector = usersCentroid.get(currUser);
            Map<String, Double> currUserMap = new HashMap<>();

            if (currUserVector != null) {
                for (String otherUser : userSet) {
                    Multimap<String, String> otherUserVector = usersCentroid.get(otherUser);
                    Double finalScore = null;
                    if (otherUserVector != null) {
                        // F1(collaborative score, content-score)
                        Double collabScore = computeCovotedItems(currUser, otherUser),
                                contentScore = function.compute(currUserVector, otherUserVector);

                        finalScore = (collabScore + contentScore) / 2;

                    }

                    currUserMap.put("U:" + otherUser, finalScore);
                    if (finalScore != null) {
                        // update minimum score similarity
                        if (finalScore < minSimilarity)
                            minSimilarity = finalScore;

                        // update sum similarity
                        sumSimilarity += finalScore;
                    }
                }

                for (String itemID : allItems) {
                    Multimap<String, String> itemVector = itemsRepresentation.get(itemID);
                    Double finalScore = null;
                    if (itemVector != null) {
                        finalScore = function.compute(currUserVector, itemVector);
                    }

                    currUserMap.put("I:" + itemID, finalScore);
                    if (finalScore != null) {
                        // update minimum score similarity
                        if (finalScore < minSimilarity)
                            minSimilarity = finalScore;

                        // total similarity
                        sumSimilarity += finalScore;
                    }

                }
            } else {
                for (String otherUser : userSet) {
                    currUserMap.put("U:" + otherUser, null);
                }

                for (String itemID : allItems) {
                    currUserMap.put("I:" + itemID, null);
                }
            }

            this.simUserMap.put(currUser, currUserMap);

            // set missed minimum similarity value for not mapped item
            int numNullFields = replaceNullFields(currUser, minSimilarity);

            sumSimilarity += numNullFields * minSimilarity;

            // normalize curr user similarities with minimum similarity
            normalizeSimilarityScore(currUser, sumSimilarity);

            sumSimilarity = 0;
            minSimilarity = Double.MAX_VALUE;

        }


    }

    private int replaceNullFields(String currUser, Double minValue) {
        int numNullFields = 0;

        Map<String, Double> currUserSim = simUserMap.get(currUser);
        for (String entityID : currUserSim.keySet()) {
            if (currUserSim.get(entityID) == null) {
                currUserSim.put(entityID, minValue);
                numNullFields++;
            }
        }

        return numNullFields;
    }

    private void normalizeSimilarityScore(String currUser, Double sumValue) {

        Map<String, Double> currUserSim = simUserMap.get(currUser);

        for (String entityID : currUserSim.keySet()) {
            currUserSim.put(entityID, currUserSim.get(entityID) / sumValue);
        }

    }

    private double computeCovotedItems(String firstUser, String secUser) {
        Set<String> firstPostems = trainingPosNeg.get(firstUser).get(0),
                secPosItems = trainingPosNeg.get(secUser).get(0);

        return (double) Sets.intersection(firstPostems, secPosItems).size() / (firstPostems.size() + secPosItems.size());

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

    private Map<String, Multimap<String, String>> getRatedItemRepresentation(Collection<String> ratedItems) {
        Map<String, Multimap<String, String>> ratedItemsRepresentation = new HashMap<>();
        for (String itemID : ratedItems) {
            Multimap<String, String> representation = this.itemsRepresentation.get(itemID);
            if (representation != null)
                ratedItemsRepresentation.put(itemID, representation);
        }

        return ratedItemsRepresentation;
    }

    private Set<Rating> profileUser(String userID, Set<String> trainingPos, Set<String> trainingNeg, Set<String> testItems) {
        Set<Rating> allRecommendation = new TreeSet<>();

        SimilarityVertexTransformer transformer = new SimilarityVertexTransformer(userID, trainingPos, trainingNeg, simUserMap);
        PageRankWithPriors<String, String> priors = new PageRankWithPriors<>(this.recGraph, transformer, 0.15);

        priors.setMaxIterations(25);
        priors.evaluate();

        for (String currItemID : testItems) {
            allRecommendation.add(new Rating(currItemID, String.valueOf(priors.getVertexScore("I:" + currItemID))));

        }

        return allRecommendation;
    }


}
