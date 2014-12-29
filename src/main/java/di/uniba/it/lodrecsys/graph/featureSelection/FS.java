package di.uniba.it.lodrecsys.graph.featureSelection;

import com.google.common.collect.ArrayListMultimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.graph.Edge;
import di.uniba.it.lodrecsys.graph.RecGraph;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by simo on 24/12/14.
 */
public abstract class FS implements Serializable {
    protected static Logger currLogger = Logger.getLogger(RecGraph.class.getName());
    protected static UndirectedSparseMultigraph<String, Edge> recGraph;
    private ArrayListMultimap<String, Set<String>> trainingPosNeg;
    private Map<String, Set<String>> testSet;
    private Map<String, String> uriIdMap;
    private Map<String, String> idUriMap;

    public FS(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) throws IOException {
        try {
            FileInputStream fis = new FileInputStream("./serialized/graphComplete.bin");
            ObjectInputStream ois = new ObjectInputStream(fis);
            recGraph = (UndirectedSparseMultigraph<String, Edge>) ois.readObject();
            ois.close();
            fis.close();
            System.out.println(new Date() + " [INFO] Graph Complete loaded.");
            System.out.println(new Date() + " [INFO] Graph Complete Vertices : " + recGraph.getVertices().size());
            System.out.println(new Date() + " [INFO] Graph Complete Edges : " + recGraph.getEdges().size());
            System.out.println("----------------------------------------------------");

        } catch (FileNotFoundException e) {
            recGraph = new UndirectedSparseMultigraph<>();
            getMapForMappedItems(mappedItems);
            generateGraph(new RequestStruct(trainingFileName, testFile, proprIndexDir, mappedItems));
            printDot(this.getClass().getSimpleName());
            save();
            System.out.println(new Date() + " [INFO] Graph Complete builded.");
            System.out.println(new Date() + " [INFO] Graph Complete Vertices : " + recGraph.getVertices().size());
            System.out.println(new Date() + " [INFO] Graph Complete Edges : " + recGraph.getEdges().size());
            System.out.println("----------------------------------------------------");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() throws IOException {
        new File("./serialized").mkdirs();
        FileOutputStream fos = new FileOutputStream("./serialized/graphComplete.bin");
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

        for (String itemID : allItemsID) {
            String resourceURI = idUriMap.get(itemID);
            if (resourceURI != null) {
                addItemProperties(propManager, resourceURI);
            }
        }
        currLogger.info(String.format("Total number of vertex %s - Total number of edges %s", recGraph.getVertexCount(), recGraph.getEdgeCount()));
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
            ed += "\" [label=\"" + namePredicate(edge.getProperty()) + "\"];";
            out.println(ed);
        }

        out.println("}");
        out.close();
        fout.close();
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
