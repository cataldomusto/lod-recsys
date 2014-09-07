package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.graph.indexer.LODIndexerReader;
import di.uniba.it.lodrecsys.graph.scorer.SimNextVertexTransformer;
import di.uniba.it.lodrecsys.graph.scorer.SimilarityVertexTransformer;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by asuglia on 8/3/14.
 */
public class UserItemCosine2 extends RecGraph {

    private Map<String, String> idUriMap;
    private Map<String, String> uriIdMap;
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;
    private PropertiesManager propManager;
    private Map<String, Map<String, Double>> simUserMap;

    public UserItemCosine2(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) {
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

        //LODIndexer.createLODIndexer(propManager, trainingPosNeg, idUriMap);


        computeSimilarityMap(allItemsID);
        Utils.serializeSimilarityMatrix(this.simUserMap, "sim.txt");
        currLogger.info("Computed similarity map");
        Double meanSimUser = computeMeanUserSimilarity();

        currLogger.info("sim(u1, i1)= " + simUserMap.get("1").get("I:1")); //p

        currLogger.info("sim(u1, i9)= " + simUserMap.get("1").get("I:9")); //n
        currLogger.info("sim(u1, i2)= " + simUserMap.get("1").get("I:2")); //p

        currLogger.info("sim(u1, i5)= " + simUserMap.get("1").get("I:5")); //n


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

    private void computeSimilarityMap(Set<String> allItems) throws IOException {
        this.simUserMap = new HashMap<>();


        LODIndexerReader indexerReader = new LODIndexerReader("lod_index");

        Set<String> userSet = trainingPosNeg.keySet();
        double norma = 0, minSimilarity = Double.MAX_VALUE;

        for (String currUser : userSet) {
            Map<String, Double> currUserMap = new HashMap<>();
            for (String otherUser : userSet) {
                currUserMap.put("U:" + otherUser, null);
            }

            for (String itemID : allItems) {
                currUserMap.put("I:" + itemID, null);
            }

            simUserMap.put(currUser, currUserMap);
        }

        //String currUser = "1";
        for (String currUser : userSet) {
            currLogger.info("Computing similarity for user:" + currUser);
            Map<String, Double> currMap = simUserMap.get(currUser);
            currMap.putAll(indexerReader.computeSimilarityUser(currUser));

            for (Double val : currMap.values()) {
                if (val != null) {
                    norma += Math.pow(val, 2);
                    if (val < minSimilarity)
                        minSimilarity = val;
                }
            }

            // set missed minimum similarity value for not mapped item
            int nullValues = replaceNullFields(currMap, minSimilarity);


            norma += Math.pow(minSimilarity, 2) * nullValues;

            norma = Math.sqrt(norma);

            // normalize matrix
            normalizeSimilarityScore(currMap, norma);

            norma = 0;
            minSimilarity = Integer.MAX_VALUE;
            simUserMap.put(currUser, currMap);
        }


    }

    private int replaceNullFields(Map<String, Double> currUserSim, Double minValue) {
        int nullValues = 0;

        for (String entityID : currUserSim.keySet()) {
            if (currUserSim.get(entityID) == null) {
                currUserSim.put(entityID, minValue);
                nullValues++;
            }
        }

        return nullValues;

    }


    private void normalizeSimilarityScore(Map<String, Double> currUserSim, Double sumValue) {
        for (String entityID : currUserSim.keySet()) {
            currUserSim.put(entityID, currUserSim.get(entityID) / sumValue);
        }

    }

    @Override
    public Map<String, Set<Rating>> runPageRank(RequestStruct requestParam) throws IOException {
        Map<String, Set<Rating>> usersRecommendation = new HashMap<>();
        // compute recommendation for all users

        for (String userID : testSet.keySet()) {
            currLogger.info("Page rank for user: " + userID);
            List<Set<String>> posNegativeRatings = trainingPosNeg.get(userID);
            Set<String> testItems = testSet.get(userID);
            usersRecommendation.put(userID, profileUser(userID, testItems));
        }

        return usersRecommendation;
    }

    private Set<Rating> profileUser(String userID, Set<String> testItems) {
        Set<Rating> allRecommendation = new TreeSet<>();

        SimilarityVertexTransformer transformer = new SimilarityVertexTransformer(userID, simUserMap);
        //SimNextVertexTransformer transformer = new SimNextVertexTransformer(userID, trainingPos, trainingNeg, simUserMap);
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
        List<Map<String, String>> metricsForSplit = new ArrayList<>();
        String completeResFile = resPath + File.separator + "UserItemCosineNorm" + File.separator + "metrics.complete";

        for (int numSplit = 1; numSplit <= 5; numSplit++) {
            UserItemCosine2 jaccard = new UserItemCosine2(testPath + File.separator + "u" + numSplit + ".base", testPath + File.separator + "u" + numSplit + ".test",
                    propertyIndexDir, mappingList);
            Map<String, Set<Rating>> ratings = jaccard.runPageRank(new RequestStruct(0.85));

            String resFile = resPath + File.separator + "UserItemCosineNorm" + File.separator + "u" + numSplit + ".result";

            EvaluateRecommendation.serializeRatings(ratings, resFile, -1);

            String trecTestFile = testTrecPath + File.separator + "u" + numSplit + ".test";
            String trecResultFinal = resFile.substring(0, resFile.lastIndexOf(File.separator))
                    + File.separator + "u" + numSplit + ".final";
            EvaluateRecommendation.saveTrecEvalResult(trecTestFile, resFile, trecResultFinal);
            metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
            currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());

        }

        EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, 5), completeResFile);

    }
}
