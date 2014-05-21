package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import org.apache.mahout.cf.taste.common.TasteException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

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

    public abstract void runPageRank(String resultFile, RequestStruct requestParam) throws IOException, TasteException;
}
