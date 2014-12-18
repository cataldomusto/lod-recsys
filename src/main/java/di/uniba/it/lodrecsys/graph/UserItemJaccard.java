package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.graph.scorer.SimilarityVertexTransformer;
import di.uniba.it.lodrecsys.properties.PropertiesCalculator;
import di.uniba.it.lodrecsys.properties.Similarity;
import di.uniba.it.lodrecsys.properties.SimilarityFunction;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class which represents a collaborative graph with
 * an enhanced weighting scheme based on Jaccard Similarity
 *
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
        Sum sumObj = new Sum();

        for (String currUser : simUserMap.keySet()) {
            Map<String, Double> currUserSim = simUserMap.get(currUser);

            for (String entityID : currUserSim.keySet()) {
                if (entityID.startsWith("U:")) {
                    Double score = currUserSim.get(entityID);
                    if (!score.isNaN())
                        sumObj.increment(score);
                }
            }
        }

        return sumObj.getResult() / (sumObj.getN() / 2);
    }


    private void computeSimilarityMap(SimilarityFunction function, Set<String> allItems) {
        this.simUserMap = new HashMap<>();

        Set<String> userSet = trainingPosNeg.keySet();
        Double minSimilarity = (double) function.getMaxValue();
        Sum sumObject = new Sum();

        for (String currUser : userSet) {
            Multimap<String, String> currUserVector = usersCentroid.get(currUser);
            Map<String, Double> currUserMap = new HashMap<>();

            if (currUserVector != null) {
                for (String otherUser : userSet) {
                    Multimap<String, String> otherUserVector = usersCentroid.get(otherUser);
                    Double finalScore = null;
                    if (otherUserVector != null) {
                        finalScore = (double) function.compute(currUserVector, otherUserVector);
                    }

                    currUserMap.put("U:" + otherUser, finalScore);
                    if (finalScore != null) {
                        // update minimum score similarity
                        if (finalScore < minSimilarity)
                            minSimilarity = finalScore;

                        // update sum similarity
                        if (!finalScore.isNaN())
                            sumObject.increment(finalScore);
                    }
                }

                for (String itemID : allItems) {
                    Multimap<String, String> itemVector = itemsRepresentation.get(itemID);
                    Double finalScore = null;

                    // If the current item has been voted positively by the current user
                    // assign "1" as a similarity score
                    if (trainingPosNeg.get(currUser).get(0).contains(itemID)) {
                        finalScore = 1d;
                    } else if (trainingPosNeg.get(currUser).get(1).contains(itemID)) {
                        finalScore = 0d;
                    } else if (itemVector != null) {
                        finalScore = (double) function.compute(currUserVector, itemVector);
                    }


                    currUserMap.put("I:" + itemID, finalScore);
                    if (finalScore != null) {
                        // update minimum score similarity
                        if (finalScore < minSimilarity)
                            minSimilarity = finalScore;


                        // update sum similarity
                        if (!finalScore.isNaN())
                            sumObject.increment(finalScore);
                    }

                }
            } else {
                for (String otherUser : userSet) {
                    currUserMap.put("U:" + otherUser, null);
                }

                for (String itemID : allItems) {
                    Double finalScore = null;

                    // If the current item has been voted positively by the current user
                    // assign "1" as a similarity score
                    if (trainingPosNeg.get(currUser).get(0).contains(itemID)) {
                        finalScore = 1d;
                    } else if (trainingPosNeg.get(currUser).get(1).contains(itemID)) {
                        finalScore = 0d;
                    }

                    currUserMap.put("I:" + itemID, finalScore);
                    if (finalScore != null) {
                        // update minimum score similarity
                        if (finalScore < minSimilarity)
                            minSimilarity = finalScore;


                        // update sum similarity
                        if (!finalScore.isNaN())
                            sumObject.increment(finalScore);
                    }

                }
            }

            this.simUserMap.put(currUser, currUserMap);

            // set missed minimum similarity value for not mapped item
            if (minSimilarity == function.getMaxValue())
                minSimilarity = 0d;
            int numNullFields = replaceNullFields(currUser, minSimilarity);


            // update sum similarity
            sumObject.increment(numNullFields * minSimilarity);
            // normalize curr user similarities with minimum similarity
            normalizeSimilarityScore(currUser, sumObject.getResult());

            sumObject.clear();
            minSimilarity = (double) function.getMaxValue();

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
        // compute recommendation for all users

        for (String userID : testSet.keySet()) {
            currLogger.info("Page rank for user: " + userID);
            Set<String> testItems = testSet.get(userID);
            usersRecommendation.put(userID, profileUser(userID, testItems));
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

    private Set<Rating> profileUser(String userID, Set<String> testItems) {
        Set<Rating> allRecommendation = new TreeSet<>();

        SimilarityVertexTransformer transformer = new SimilarityVertexTransformer(userID, simUserMap);
        PageRankWithPriors<String, String> priors = new PageRankWithPriors<>(this.recGraph, transformer, 0.15);

        priors.setMaxIterations(25);
        priors.evaluate();

        for (String currItemID : testItems) {
            allRecommendation.add(new Rating(currItemID, String.valueOf(priors.getVertexScore("I:" + currItemID))));

        }

        return allRecommendation;
    }


    public static void main(String[] args) throws IOException {
        String trainPath = "/home/asuglia/thesis/dataset/ml-100k/definitive",
                testPath = "/home/asuglia/thesis/dataset/ml-100k/binarized",
                testTrecPath = "/home/asuglia/thesis/dataset/ml-100k/trec",
                resPath = "/home/asuglia/thesis/dataset/ml-100k/results",
                propertyIndexDir = "/home/asuglia/thesis/content_lodrecsys/movielens/stored_prop",
                tagmeDir = "/home/asuglia/thesis/content_lodrecsys/movielens/tagme",
                mappedItemFile = "mapping/item.mapping";

        List<MovieMapping> mappingList = Utils.loadDBpediaMappedItems(mappedItemFile);
        long meanTimeElapsed = 0, startTime;

        for (int numSplit = 1; numSplit <= 5; numSplit++) {
            startTime = System.nanoTime();
            UserItemJaccard graph = new UserItemJaccard(testPath + File.separator + "u" + numSplit + ".base", testPath + File.separator + "u" + numSplit + ".test",
                    propertyIndexDir, mappingList);
            Map<String, Set<Rating>> ratings = graph.runPageRank(new RequestStruct(0.85));
            meanTimeElapsed += (System.nanoTime() - startTime);
        }

        meanTimeElapsed /= 5;
        currLogger.info("Total running time: " + meanTimeElapsed);

    }


}
