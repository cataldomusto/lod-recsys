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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Class which represents the user-item-lod-lodexp
 * configuration
 */
public class UserItemOneExp extends RecGraph {
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;
    private Map<String, String> uriIdMap;
    private Map<String, String> idUriMap;

    public UserItemOneExp(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) {
        try {
            getMapForMappedItems(mappedItems);
            generateGraph(new RequestStruct(trainingFileName, testFile, proprIndexDir, mappedItems));
            printDot("UserItemOneExp");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    public static void main(String[] args) throws IOException {
//        String trainPath = "/home/asuglia/thesis/dataset/ml-100k/definitive",
//                testPath = "/home/asuglia/thesis/dataset/ml-100k/binarized",
//                testTrecPath = "/home/asuglia/thesis/dataset/ml-100k/trec",
//                resPath = "/home/asuglia/thesis/dataset/ml-100k/results",
//                propertyIndexDir = "/home/asuglia/thesis/content_lodrecsys/movielens/stored_prop",
//                tagmeDir = "/home/asuglia/thesis/content_lodrecsys/movielens/tagme",
//                mappedItemFile = "mapping/item.mapping";
//
//        List<MovieMapping> mappingList = Utils.loadDBpediaMappedItems(mappedItemFile);
//        long meanTimeElapsed = 0, startTime;
//
//        for (int numSplit = 1; numSplit <= 5; numSplit++) {
//            startTime = System.nanoTime();
//            UserItemOneExp graph = new UserItemOneExp(testPath + File.separator + "u" + numSplit + ".base", testPath + File.separator + "u" + numSplit + ".test",
//                    propertyIndexDir, mappingList);
//            Map<String, Set<Rating>> ratings = graph.runPageRank(new RequestStruct(0.85));
//            meanTimeElapsed += (System.nanoTime() - startTime);
//        }
//
//        meanTimeElapsed /= 5;
//        currLogger.info("Total running time: " + meanTimeElapsed);
//
//    }

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

        PropertiesManager propManager = new PropertiesManager((String) requestStruct.params.get(2));
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

        for (String itemID : allItemsID) {
            String resourceURI = idUriMap.get(itemID);
            if (resourceURI == null)
                recGraph.addVertex(itemID);
            else {
                recGraph.addVertex(resourceURI);
                addItemProperties(itemID, propManager, resourceURI);
            }
        }


        for (String userID : trainingPosNeg.keySet()) {
            int edgeCounter = 0;

            for (String posItemID : trainingPosNeg.get(userID).get(0)) {
                String resourceURI = idUriMap.get(posItemID);
                if (resourceURI == null) {

                    Edge e = new Edge(userID + "-" + edgeCounter, "U:" + userID, posItemID);
                    recGraph.addEdge(e, e.getSubject(), e.getObject());
                } else {
                    Edge e = new Edge(userID + "-" + edgeCounter, "U:" + userID, resourceURI);
                    recGraph.addEdge(e, e.getSubject(), e.getObject());
                }
                edgeCounter++;

            }

        }

        currLogger.info(String.format("Total number of vertex %s - Total number of edges %s", recGraph.getVertexCount(), recGraph.getEdgeCount()));

    }

    private void printDot(String name) throws IOException {
        new File("./dot").mkdirs();
        FileOutputStream fout = new FileOutputStream("./dot/graph" + name + ".dot");
        PrintWriter out = new PrintWriter(fout);
        out.println("graph " + name + " {");

        for (String s : recGraph.getVertices()) {
            out.println("\"" + s + "\" [shape=box];");
        }
        out.println();

        for (Edge edge : recGraph.getEdges()) {
            String ed = "\"" + edge.getSubject() + "\" -- \"" + edge.getObject();
            ed += "\" [label=\"" + edge.getProperty() + "\"];";
            out.println(ed);
        }

        out.println("}");
        out.close();
        fout.close();
    }

    private void addItemProperties(String itemID, PropertiesManager propManager, String resourceURI) {

        List<Statement> resProperties = propManager.getResourceProperties(resourceURI);
        long i = 1;

        for (Statement stat : resProperties) {
            String object = stat.getObject().toString();
            List<Statement> objExpansionProp = propManager.getResourceProperties(object);
            int j = 0;
            for (Statement statExp : objExpansionProp) {
                Edge e = new Edge(namePredicate(statExp) + "-prop_one-" + (j++), object, statExp.getObject().toString());
                recGraph.addEdge(e, e.getSubject(), e.getObject());
            }

            Edge e = new Edge(namePredicate(stat), resourceURI, object);
            recGraph.addEdge(e, e.getSubject(), e.getObject());
        }

    }

    private String namePredicate(Statement stat) {
        return stat.getPredicate().toString().replace("http://dbpedia.org/resource/", "")
                .replace("http://dbpedia.org/ontology/", "")
                .replace("http://purl.org/dc/terms/", "")
                .replace("http://dbpedia.org/property/", "")
                .replace("http://dbpedia.org/resource/", "")
                .replace(".", "_").replace(":", "_").replace(",", "_").replace("-", "_");
    }

    @Override
    public Map<String, Set<Rating>> runPageRank(RequestStruct requestParam) {
        Map<String, Set<Rating>> usersRecommendation = new HashMap<>();

        double massProb = (double) requestParam.params.get(0); // max proportion of positive items for user

        // compute recommendation for all users

        for (String userID : testSet.keySet()) {
//            currLogger.info("Page rank for user: " + userID);
            List<Set<String>> posNegativeRatings = trainingPosNeg.get(userID);
            Set<String> testItems = testSet.get(userID);
            usersRecommendation.put(userID, profileUser(userID, posNegativeRatings.get(0), posNegativeRatings.get(1), testItems, massProb));
        }

        return usersRecommendation;
    }

    private Set<Rating> profileUser(String userID, Set<String> trainingPos, Set<String> trainingNeg, Set<String> testItems, double massProb) {
        Set<Rating> allRecommendation = new TreeSet<>();

        SimpleVertexTransformer transformer = new SimpleVertexTransformer(trainingPos, trainingNeg, this.recGraph.getVertexCount(), massProb, uriIdMap);
        PageRankWithPriors<String, Edge> priors = new PageRankWithPriors<>(this.recGraph, transformer, 0.15);

        priors.setMaxIterations(25);
        priors.evaluate();

        for (String currItemID : testItems) {
            String resourceURI = idUriMap.get(currItemID);
            if (resourceURI == null)
                allRecommendation.add(new Rating(currItemID, String.valueOf(priors.getVertexScore(currItemID))));
            else
                allRecommendation.add(new Rating(currItemID, String.valueOf(priors.getVertexScore(resourceURI))));
        }

        return allRecommendation;
    }

}
