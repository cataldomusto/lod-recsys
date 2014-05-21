package di.uniba.it.lodrecsys.graph;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;

import java.io.IOException;

/**
 * Created by asuglia on 5/21/14.
 */
public abstract class RecGraph {
    protected Graph<String, String> recGraph;

    public RecGraph() {
        recGraph = new DirectedSparseMultigraph<>();

    }

    public RecGraph(String trainingFile) throws IOException {
        recGraph = new DirectedSparseGraph<>();
        try {
            generateGraph(trainingFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public abstract void generateGraph(String trainingFileName) throws IOException;

}
