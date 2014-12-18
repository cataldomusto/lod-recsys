package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * An abstract class which represents the generic structure
 * for a recommendation graph on which could be run Personalized PageRank
 */
public abstract class RecGraph {
    protected static Logger currLogger = Logger.getLogger(RecGraph.class.getName());

    protected Graph<String, String> recGraph;

    public RecGraph() {
        recGraph = new UndirectedSparseMultigraph<>();
    }

    /**
     * Constructs the graph according to the request passed
     * as a parameter
     *
     * @param requestStruct the parameters needed in order to construct the graph
     * @throws IOException if some wrong operations are done on the request's data
     */
    public abstract void generateGraph(RequestStruct requestStruct) throws IOException;

    /**
     * Executes personalized PageRank and returns recommendation
     * for each user in the test set
     *
     * @param requestParam Parameters needed for the algorithm
     * @return recommendation list for each user in the test set
     * @throws IOException if something is wrong during the algorithm execution
     */
    public abstract Map<String, Set<Rating>> runPageRank(RequestStruct requestParam) throws IOException;


}
