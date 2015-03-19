package di.uniba.it.lodrecsys.graph.featureSelection;

import com.google.common.collect.ArrayListMultimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.graph.Edge;
import di.uniba.it.lodrecsys.graph.RecGraph;
import di.uniba.it.lodrecsys.utils.CmdExecutor;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import static di.uniba.it.lodrecsys.graph.GraphRecRun.savefileLog;

/**
 * Created by Simone Rutigliano on 24/12/14.
 */
public abstract class FS implements Serializable {
    protected static Logger currLogger = Logger.getLogger(RecGraph.class.getName());
    protected static UndirectedSparseMultigraph<String, Edge> recGraph;
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;
    private Map<String, String> uriIdMap;
    private Map<String, String> idUriMap;

    public FS(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) throws IOException {
        savefileLog("");
        savefileLog("--- Loading or building graph complete...");
        try {
            FileInputStream fis = new FileInputStream(LoadProperties.DATASETPATH + "/serialized/graphComplete.bin");
            ObjectInputStream ois = new ObjectInputStream(fis);
            recGraph = (UndirectedSparseMultigraph<String, Edge>) ois.readObject();
            ois.close();
            fis.close();
            savefileLog(new Date() + " [INFO] Graph Complete loaded.");
            savefileLog(new Date() + " [INFO] Graph Complete Vertices : " + recGraph.getVertices().size());
            savefileLog(new Date() + " [INFO] Graph Complete Edges : " + recGraph.getEdges().size());
            savefileLog("-----------------------------------------------------");

        } catch (FileNotFoundException e) {
            recGraph = new UndirectedSparseMultigraph<>();
            getMapForMappedItems(mappedItems);
            generateGraph(new RequestStruct(trainingFileName, testFile, proprIndexDir, mappedItems));
            printDot(this.getClass().getSimpleName());
            save();
            savefileLog(new Date() + " [INFO] Graph Complete builded.");
            savefileLog(new Date() + " [INFO] Graph Complete Vertices : " + recGraph.getVertices().size());
            savefileLog(new Date() + " [INFO] Graph Complete Edges : " + recGraph.getEdges().size());
            savefileLog("-----------------------------------------------------");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() throws IOException {
        new File(LoadProperties.DATASETPATH + "/serialized").mkdirs();
        FileOutputStream fos = new FileOutputStream(LoadProperties.DATASETPATH + "/serialized/graphComplete.bin");
        ObjectOutputStream o = new ObjectOutputStream(fos);
        o.writeObject(recGraph);
        o.close();
        fos.close();
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

        extractRating(trainingFileName);

        for (String itemID : allItemsID) {
            String resourceURI = idUriMap.get(itemID);
            if (resourceURI != null) {
                addItemProperties(propManager, resourceURI);
            }
        }
        currLogger.info(String.format("Total number of vertex %s - Total number of edges %s", recGraph.getVertexCount(), recGraph.getEdgeCount()));
    }

    private void printDot(String name) throws IOException {
        new File(LoadProperties.DATASETPATH + "/dot").mkdirs();
        FileOutputStream fout = new FileOutputStream(LoadProperties.DATASETPATH + "/graph" + name + ".dot");
        PrintWriter out = new PrintWriter(fout);
        out.println("graph " + name + " {");

        for (String s : recGraph.getVertices()) {
            out.println("\"" + s + "\" [shape=box];");
        }
        out.println();

        for (Edge edge : recGraph.getEdges()) {
            String ed = "\"" + edge.getSubject() + "\" -- \"" + edge.getObject();
            ed += "\" [label=\"" + namePredicate(edge.getProperty()) + "\"];";
            out.println(ed);
        }

        out.println("}");
        out.close();
        fout.close();
    }

    private void extractRating(String trainingFileName) {
        if (trainingFileName.contains("given_all")) {
            new File(LoadProperties.DATASETPATH + "/serialized/").mkdirs();
            FileOutputStream foutUser = null;
            try {
                foutUser = new FileOutputStream(LoadProperties.DATASETPATH + "/serialized/rates4usersTEMP");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            PrintWriter outUser = new PrintWriter(foutUser);

            FileOutputStream foutItem = null;
            try {
                foutItem = new FileOutputStream(LoadProperties.DATASETPATH + "/serialized/rates4itemsTEMP");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            PrintWriter outItem = new PrintWriter(foutItem);


            HashSet<String> items = new HashSet<>();
            trainingPosNeg.removeAll("DBbook_userID");
            int tot = 0;
//        // Extract rating for all users
            for (String userID : trainingPosNeg.keySet()) {
                outUser.println(userID + " Positive: " + trainingPosNeg.get(userID).get(0).size() +
                                " Negative:  " + trainingPosNeg.get(userID).get(1).size() +
                                " TOT: " + (trainingPosNeg.get(userID).get(0).size() +
                                trainingPosNeg.get(userID).get(1).size())
                );

                tot += (trainingPosNeg.get(userID).get(0).size() + trainingPosNeg.get(userID).get(1).size());

                for (String s : trainingPosNeg.get(userID).get(0))
                    items.add(s);

                for (String s : trainingPosNeg.get(userID).get(1))
                    items.add(s);
            }

            outUser.println("USER : " + trainingPosNeg.keySet().size() + " TOT: " + tot);

            // Extract rating for all items
            tot = 0;
            for (String item : items) {
                int pos = 0;
                int neg = 0;
                for (String userID : trainingPosNeg.keySet()) {
                    if (trainingPosNeg.get(userID).get(0).contains(item))
                        pos++;
                    if (trainingPosNeg.get(userID).get(1).contains(item))
                        neg++;
                }
                outItem.println(item + " Positive: " + pos +
                                " Negative:  " + neg +
                                " TOT: " + (pos + neg)
                );
                tot += pos + neg;
            }
            outItem.println("ITEMS: " + items.size() + " TOT: " + tot);
            outItem.close();
            outUser.close();
            try {
                foutItem.close();
                foutUser.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        CmdExecutor.executeCommand("sort -g " + LoadProperties.DATASETPATH + "/serialized/rates4usersTEMP > " + LoadProperties.DATASETPATH + "/serialized/rates4users", false);
        CmdExecutor.executeCommand("rm " + LoadProperties.DATASETPATH + "/serialized/rates4usersTEMP", false);
        CmdExecutor.executeCommand("sort -g " + LoadProperties.DATASETPATH + "/serialized/rates4itemsTEMP > " + LoadProperties.DATASETPATH + "/serialized/rates4items", false);
        CmdExecutor.executeCommand("rm " + LoadProperties.DATASETPATH + "/serialized/rates4itemsTEMP", false);
        System.exit(3);
    }

    private void addItemProperties(PropertiesManager propManager, String resourceURI) {
        List<Statement> resProperties = propManager.getResourceProperties(resourceURI);
        for (Statement stat : resProperties) {
            String object = stat.getObject().toString();
            Edge e = new Edge(stat.getPredicate().toString(), resourceURI, object);
            recGraph.addVertex(e.getObject());
            recGraph.addVertex(e.getSubject());
            recGraph.addEdge(e, e.getSubject(), e.getObject());
        }
    }

    private String namePredicate(String prop) {
        return prop.replace("http://dbpedia.org/resource/", "")
                .replace("http://dbpedia.org/ontology/", "")
                .replace("http://purl.org/dc/terms/", "")
                .replace("http://dbpedia.org/property/", "")
                .replace("http://dbpedia.org/resource/", "")
                .replace(".", "_").replace(":", "_").replace(",", "_").replace("-", "_");
    }

    public abstract void run() throws IOException;
}
