package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.graph.scorer.SimpleVertexTransformer;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;

import java.io.*;
import java.util.*;

import static di.uniba.it.lodrecsys.graph.GraphRecRun.savefileLog;

/**
 * Class which represents the user-item-lod
 * configuration
 */
public class UserItemExpDBPedia extends RecGraph implements Serializable {
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;
    private Map<String, String> uriIdMap;
    private Map<String, String> idUriMap;

    public UserItemExpDBPedia(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) {
        try {
            getMapForMappedItems(mappedItems);
            generateGraph(new RequestStruct(trainingFileName, testFile, proprIndexDir, mappedItems));
            printDot(this.getClass().getSimpleName());
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

        String films = "";
        String trainingFileName = (String) requestStruct.params.get(0),
                testFile = (String) requestStruct.params.get(1);

        PropertiesManager propManager = new PropertiesManager((String) requestStruct.params.get(2));
        List<MovieMapping> mappedItemsList = (List<MovieMapping>) requestStruct.params.get(3);
        getMapForMappedItems(mappedItemsList);


        trainingPosNeg = Utils.loadPosNegRatingForEachUser(trainingFileName);
        testSet = Utils.loadRatedItems(new File(testFile), false);
        Set<String> allItemsID = new TreeSet<>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(LoadProperties.CHOOSENPROP));
        String prop;
        ArrayList<String> propertiesChoosen = new ArrayList<>(40);
        while ((prop = bufferedReader.readLine()) != null) {
            propertiesChoosen.add(prop);
        }
        bufferedReader.close();

        for (Set<String> items : testSet.values()) {
            allItemsID.addAll(items);
        }

        for (String userID : trainingPosNeg.keySet()) {
            allItemsID.addAll(trainingPosNeg.get(userID).get(0));

        }

        for (String itemID : allItemsID) {
            String resourceURI = idUriMap.get(itemID);
            if (resourceURI == null) {
                recGraph.addVertex(itemID);
            } else {
                recGraph.addVertex(resourceURI);
                addItemProperties(propertiesChoosen, propManager, resourceURI);
                films += resourceURI + "\n";
            }
        }


        for (String userID : trainingPosNeg.keySet()) {
            int edgeCounter = 0;

            for (String posItemID : trainingPosNeg.get(userID).get(0)) {
                String resourceURI = idUriMap.get(posItemID);
                if (resourceURI == null) {
                    Edge edge = new Edge(userID + "-" + edgeCounter, "U:" + userID, posItemID);
                    recGraph.addEdge(edge, edge.getSubject(), edge.getObject());
                } else {
                    Edge e = new Edge(userID + "-" + edgeCounter, "U:" + userID, resourceURI);
                    recGraph.addEdge(e, e.getSubject(), e.getObject());
                }
                edgeCounter++;

            }

        }
        savefileLog(new Date() + " [INFO] Graph Filtered builded.");
        savefileLog(new Date() + " [INFO] Graph Filtered Vertices : " + recGraph.getVertexCount());
        savefileLog(new Date() + " [INFO] Graph Filtered Edges : " + recGraph.getEdgeCount());
        savefileLog("-----------------------------------------------------");
        save();
//        currLogger.info(String.format("Total number of vertex %s - Total number of edges %s", recGraph.getVertexCount(), recGraph.getEdgeCount()));
    }

    public static void save() throws IOException {
        new File("./serialized").mkdirs();
        String nameF = LoadProperties.FILTERTYPE;
        if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
            nameF += LoadProperties.EVALWEKA;
        nameF += LoadProperties.NUMFILTER;
        int i = 1;
        while (new File("./serialized/graph" + nameF + "Split" + i + ".bin").exists() && i > LoadProperties.NUMSPLIT)
            i++;

        FileOutputStream fos = new FileOutputStream("./serialized/graph" + nameF + "Split" + i + ".bin");
        ObjectOutputStream o = new ObjectOutputStream(fos);
        o.writeObject(recGraph);
        o.close();
        fos.close();
    }

    private void printDot(String name) throws IOException {
        new File("./datasets/ml-100k/dot").mkdirs();
        FileOutputStream fout = new FileOutputStream("./datasets/ml-100k/dot/graph" + name + ".dot");
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

    private void addItemProperties(ArrayList<String> properties, PropertiesManager propManager, String resourceURI) throws IOException {
        List<Statement> resProperties = propManager.getResourceProperties(resourceURI);
        for (Statement stat : resProperties) {
            String object = stat.getObject().toString();
            if (properties.contains(stat.getPredicate().toString())) {
                Edge e = new Edge(stat.getPredicate().toString(), resourceURI, object);
                recGraph.addEdge(e, e.getSubject(), e.getObject());
            }

        }

    }

    @Override
    public Map<String, Set<Rating>> runPageRank(RequestStruct requestParam) throws IOException {

        Map<String, Set<Rating>> usersRecommendation = new HashMap<>();

        double massProb = (double) requestParam.params.get(0); // max proportion of positive items for user

        // compute recommendation for all users
        float totUser = testSet.size();
        float i = 0;

        for (String userID : testSet.keySet()) {

            i++;

            int onePercent = Math.round(totUser * 1) / 100;
            int twentyfivePercent = Math.round(totUser * 25) / 100;
            int fiftyPercent = Math.round(totUser * 50) / 100;
            int seventyfivePercent = Math.round(totUser * 75) / 100;

            if (i == onePercent)
                savefileLog(new Date() + " [INFO] Recommended users: 1%");
            if (i == twentyfivePercent)
                savefileLog(new Date() + " [INFO] Recommended users: 25%");
            if (i == fiftyPercent)
                savefileLog(new Date() + " [INFO] Recommended users: 50%");
            if (i == seventyfivePercent)
                savefileLog(new Date() + " [INFO] Recommended users: 75%");

//            currLogger.info("Page rank for user: " + userID);
            List<Set<String>> posNegativeRatings = trainingPosNeg.get(userID);
            Set<String> testItems = testSet.get(userID);
            usersRecommendation.put(userID, profileUser(posNegativeRatings.get(0), posNegativeRatings.get(1), testItems, massProb));
        }

        return usersRecommendation;
    }

    private Set<Rating> profileUser(Set<String> trainingPos, Set<String> trainingNeg, Set<String> testItems, double massProb) {
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
