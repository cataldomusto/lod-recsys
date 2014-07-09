package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import edu.uci.ics.jung.graph.*;
import org.apache.mahout.cf.taste.common.TasteException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by asuglia on 5/21/14.
 */
public abstract class RecGraph {
    protected static Logger currLogger = Logger.getLogger(RecGraph.class.getName());

    protected Graph<String, String> recGraph;

    public RecGraph() {
        recGraph = new UndirectedSparseMultigraph<>();//WRONG: new DirectedSparseMultigraph<>();

    }


    /*
    public RecGraph(String trainingFile, String testFile) throws IOException {
        recGraph = new UndirectedSparseMultigraph<>(); //WRONG: new DirectedSparseGraph<>();
        try {
            generateGraph(trainingFile, testFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    } */

    //public abstract void generateGraph(String trainingFileName, String testFile) throws IOException;

    public abstract void generateGraph(RequestStruct requestStruct) throws IOException;

    public abstract Map<String, Set<Rating>> runPageRank(RequestStruct requestParam) throws IOException;


}
