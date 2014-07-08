package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.graph.scorer.SimpleVertexTransformer;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import org.apache.mahout.cf.taste.common.TasteException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by asuglia on 7/8/14.
 */
public class UserItemPropTag extends RecGraph {
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;

    public UserItemPropTag(String trainingFileName, String testFileName, String proprIndexDir,
                           List<MovieMapping> mappedItems, String tagMeDir) throws IOException {
        generateGraph(new RequestStruct(trainingFileName, testFileName, proprIndexDir, mappedItems, tagMeDir));

    }

    private Map<String, List<String>> loadTagmeConcepts(String tagmeDir) {
        Map<String, List<String>> tagmeConcepts;

        try {
            tagmeConcepts = Utils.loadTAGmeConceptsForItems(tagmeDir);
        } catch (IOException e) {
            tagmeConcepts = new HashMap<>(); // unable to load tagme concepts
        }

        return tagmeConcepts;
    }

    private Map<String, String> getMapForMappedItems(List<MovieMapping> movieList) {
        // key: item-id - value: dbpedia uri
        Map<String, String> mappedItems = new HashMap<>();

        for (MovieMapping movie : movieList)
            mappedItems.put(movie.getItemID(), movie.getDbpediaURI());

        return mappedItems;
    }

    @Override
    public void generateGraph(RequestStruct requestStruct) throws IOException {
        String trainingFileName = (String) requestStruct.params.get(0),
                testFile = (String) requestStruct.params.get(1);

        PropertiesManager propManager = new PropertiesManager((String) requestStruct.params.get(2));
        List<MovieMapping> mappedItemsList = (List<MovieMapping>) requestStruct.params.get(3);
        Map<String, String> mappedItems = getMapForMappedItems(mappedItemsList);
        Map<String, List<String>> tagmeConcepts = Utils.loadTAGmeConceptsForItems((String) requestStruct.params.get(4));

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
            addItemProperties(itemID, propManager, mappedItems);
            addItemTAGmeConcepts(itemID, tagmeConcepts);
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

    private void addItemTAGmeConcepts(String itemID, Map<String, List<String>> tagmeConcepts) {
        int i = 0;

        for (String tagmeRes : tagmeConcepts.get(itemID)) {
            recGraph.addEdge(itemID + "-tagme" + i++, itemID, tagmeRes);
        }

    }

    private void addItemProperties(String itemID, PropertiesManager propManager, Map<String, String> mappedItems) {
        String resourceURI = mappedItems.get(itemID);

        List<Statement> resProperties = propManager.getResourceProperties(resourceURI);
        long i = 1;

        for (Statement stat : resProperties) {
            String object = stat.getObject().toString();
            recGraph.addEdge(itemID + "-prop" + i++, itemID, object);

        }


    }

    @Override
    public Map<String, Set<Rating>> runPageRank(RequestStruct requestParam) throws TasteException {
        Map<String, Set<Rating>> usersRecommendation = new HashMap<>();

        double massProb = (double) requestParam.params.get(0); // max proportion of positive items for user

        // compute recommendation for all users

        for (String userID : testSet.keySet()) {
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
            allRecommendation.add(new Rating(currItemID, String.valueOf(priors.getVertexScore(currItemID))));

        }

        return allRecommendation;
    }
}
