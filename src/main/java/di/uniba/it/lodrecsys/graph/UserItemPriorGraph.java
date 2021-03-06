package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.graph.scorer.SimpleVertexTransformer;
import di.uniba.it.lodrecsys.utils.Utils;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import org.apache.mahout.cf.taste.common.TasteException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Class which represents the user-item configuration
 * (Collaborative graph)
 */
public class UserItemPriorGraph extends RecGraph {
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;

    public UserItemPriorGraph(String trainingFileName, String testFile) throws IOException {
        generateGraph(new RequestStruct(trainingFileName, testFile));

    }

    @Override
    public void generateGraph(RequestStruct requestStruct) throws IOException {
        String trainingFileName = (String) requestStruct.params.get(0),
                testFile = (String) requestStruct.params.get(1);

        trainingPosNeg = Utils.loadPosNegRatingForEachUser(trainingFileName);
        testSet = Utils.loadRatedItems(new File(testFile), false);

        // Loads all the items rated in the test set
        for (Set<String> ratings : testSet.values()) {
            for (String rate : ratings)
                recGraph.addVertex(rate);
        }

        for (String userID : trainingPosNeg.keySet()) {
            int edgeCounter = 0;

            for (String posItemID : trainingPosNeg.get(userID).get(0)) {
                recGraph.addEdge(userID + "-" + edgeCounter, "U:" + userID, posItemID);
                edgeCounter++;

            }

        }

        currLogger.info("Vertex: " + recGraph.getVertexCount() + " Edges: " + recGraph.getEdgeCount());

    }

    @Override
    public Map<String, Set<Rating>> runPageRank(RequestStruct requestParam) {
        Map<String, Set<Rating>> usersRecommendation = new HashMap<>();

        double massProb = (double) requestParam.params.get(0); // max proportion of positive items for user

        // print recommendation for all users

        for (String userID : testSet.keySet()) {
            int i = 0;
            currLogger.info("Page rank for user: " + userID);
            List<Set<String>> posNegativeRatings = trainingPosNeg.get(userID);
            Set<String> testItems = testSet.get(userID);
            usersRecommendation.put(userID, profileUser(userID, posNegativeRatings.get(0), posNegativeRatings.get(1), testItems, massProb));

        }

        return usersRecommendation;
    }


    private Set<Rating> profileUser(String userID, Set<String> trainingPos, Set<String> trainingNeg, Set<String> testItems, double massProb) {
        Set<Rating> allRecommendation = new TreeSet<>();

        SimpleVertexTransformer transformer = new SimpleVertexTransformer(trainingPos, trainingNeg, this.recGraph.getVertexCount(), massProb);
        PageRankWithPriors<String, String> priors = new PageRankWithPriors<>(this.recGraph, transformer, 0.15);

        priors.setMaxIterations(25);
        priors.evaluate();

        for (String currItemID : testItems) {
            allRecommendation.add(new Rating(currItemID, priors.getVertexScore(currItemID) + ""));

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

        //List<MovieMapping> mappingList = Utils.loadDBpediaMappedItems(mappedItemFile);
        long meanTimeElapsed = 0, startTime;

        for (int numSplit = 1; numSplit <= 5; numSplit++) {
            startTime = System.nanoTime();
            UserItemPriorGraph graph = new UserItemPriorGraph(testPath + File.separator + "u" + numSplit + ".base", testPath + File.separator + "u" + numSplit + ".test");
            Map<String, Set<Rating>> ratings = graph.runPageRank(new RequestStruct(0.85));
            meanTimeElapsed += (System.nanoTime() - startTime);
        }

        meanTimeElapsed /= 5;
        currLogger.info("Total running time: " + meanTimeElapsed);

    }

}
