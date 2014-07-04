package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.graph.scorer.SimpleVertexTransformer;
import di.uniba.it.lodrecsys.graph.scorer.WeightedVertexTransformer;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import org.apache.mahout.cf.taste.common.TasteException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by asuglia on 7/1/14.
 */
public class UserItemProperty extends RecGraph {
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;
    private PropertiesManager propManager;
    private Map<String, String> mappedItems;

    public UserItemProperty(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) {

        try {
            getMapForMappedItems(mappedItems);
            propManager = new PropertiesManager(proprIndexDir);
            generateGraph(trainingFileName, testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getMapForMappedItems(List<MovieMapping> movieList) {
        // key: item-id - value: dbpedia uri
        this.mappedItems = new HashMap<>();

        for (MovieMapping movie : movieList)
            mappedItems.put(movie.getItemID(), movie.getDbpediaURI());

    }

    @Override
    public void generateGraph(String trainingFileName, String testFile) throws IOException {
        trainingPosNeg = Utils.loadPosNegRatingForEachUser(trainingFileName);
        testSet = Utils.loadRatedItems(new File(testFile), false);
        Set<String> allItemsID = new TreeSet<>();

        for (Set<String> items : testSet.values()) {
            allItemsID.addAll(items);
        }

        for (String userID : trainingPosNeg.keySet()) {
            allItemsID.addAll(trainingPosNeg.get(userID).get(0));

        }

        for (String itemID : allItemsID) {
            recGraph.addVertex(itemID);
            addItemProperties(itemID);
        }


        for (String userID : trainingPosNeg.keySet()) {
            int edgeCounter = 0;

            for (String posItemID : trainingPosNeg.get(userID).get(0)) {
                recGraph.addEdge(userID + "-" + edgeCounter, "U:" + userID, posItemID);
                edgeCounter++;

            }

        }

        currLogger.info(String.format("Total number of vertex %s - Total number of edges %s", recGraph.getVertexCount(), recGraph.getEdgeCount()));

    }

    private void addItemProperties(String itemID) {
        String resourceURI = this.mappedItems.get(itemID);

        List<Statement> resProperties = propManager.getResourceProperties(resourceURI);
        long i = 1;

        for (Statement stat : resProperties) {
            recGraph.addEdge(itemID + "-prop" + i++, itemID, stat.getObject().toString());

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
