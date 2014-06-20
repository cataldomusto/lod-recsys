package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
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
 * Created by asuglia on 5/26/14.
 */
public class UserItemPriorGraph extends RecGraph {
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;

    public UserItemPriorGraph(String trainingFileName, String testFile) {
        try {
            generateGraph(trainingFileName, testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void generateGraph(String trainingFileName, String testFile) throws IOException {
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

    }

    @Override
    public void runPageRank(String resultFile, RequestStruct requestParam) throws IOException, TasteException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(resultFile));
            int numRec = (int) requestParam.params.get(0); // number of recommendation
            double massProb = (double) requestParam.params.get(1); // max proportion of positive items for user

            // print recommendation for all users

            for (String userID : testSet.keySet()) {
                int i = 0;
                currLogger.info("Page rank for user: " + userID);
                List<Set<String>> posNegativeRatings = trainingPosNeg.get(userID);
                Set<String> testItems = testSet.get(userID);
                Set<Rating> recommendations = profileUser(userID, posNegativeRatings.get(0), posNegativeRatings.get(1), testItems, numRec, massProb);
                serializeRatings(userID, recommendations, writer);
            }


        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (writer != null) {
                writer.close();
            }

        }
    }


    private Set<Rating> profileUser(String userID, Set<String> trainingPos, Set<String> trainingNeg, Set<String> testItems, int numRec, double massProb) {
        Set<Rating> recommendation = new TreeSet<>();

        SimpleVertexTransformer transformer = new SimpleVertexTransformer(trainingPos, trainingNeg, this.recGraph.getVertexCount(), massProb);
        //UserVertexTransformer transformer = new UserVertexTransformer(trainingPos, trainingNeg, this.recGraph.getVertexCount(), massProb, userID);
        PageRankWithPriors<String, String> priors = new PageRankWithPriors<>(this.recGraph, transformer, 0.15);

        priors.setMaxIterations(25);
        priors.evaluate();

        int i = 0;

        for (String currItemID : testItems) {
            recommendation.add(new Rating(currItemID, priors.getVertexScore(currItemID) + ""));

            if (++i == numRec)
                break;
        }

        return recommendation;
    }


}
