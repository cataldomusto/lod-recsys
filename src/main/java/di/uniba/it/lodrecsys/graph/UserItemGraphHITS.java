package di.uniba.it.lodrecsys.graph;

import com.sun.jersey.core.util.StringIgnoreCaseKeyComparator;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.utils.Utils;
import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.algorithms.scoring.PageRank;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class which represents a collaborative graph (user-item)
 * which produces recommendation according the classic PageRank
 * implementation
 */
public class UserItemGraphHITS extends RecGraph {
    private Map<String, Set<Rating>> trainingSet;
    private Map<String, Set<String>> testSet;

    public UserItemGraphHITS(String trainingFileName, String testFile) throws IOException {
        generateGraph(new RequestStruct(trainingFileName, testFile));
    }


    @Override
    public void generateGraph(RequestStruct requestStruct) throws IOException {
        String trainingFileName = (String) requestStruct.params.get(0),
                testFile = (String) requestStruct.params.get(1);

        trainingSet = Utils.loadRatingForEachUser(trainingFileName);
        testSet = Utils.loadRatedItems(new File(testFile), false);

        // Loads all the items rated in the test set
        for (Set<String> ratings : testSet.values()) {
            for (String rate : ratings)
                recGraph.addVertex(rate);
        }

        for (String userID : trainingSet.keySet()) {
            // for each rating in the user's rating set
            // selects only the positive training ratings (1)
            int edgeCounter = 0; // counts the number of edges from the current UserNode

            // creates connections between the current user and the item
            // that he has rated positively
            for (Rating rate : trainingSet.get(userID)) {
                if (rate.getRating().equals("1")) {
                    recGraph.addEdge(userID + "-" + edgeCounter, "U:" + userID, rate.getItemID());
                    edgeCounter++;
                }

            }

        }

    }

    @Override
    public Map<String, Set<Rating>> runPageRank(RequestStruct requestParam) {

        Map<String, Set<Rating>> recommendationList = new HashMap<>();
        HITS<String, String> hits = new HITS<>(this.recGraph,0.15);
        hits.setMaxIterations(25);
        hits.evaluate();

        // print recommendation for all users

        for (String userID : testSet.keySet()) {
            int totElement = 0;
            Set<Rating> pageRankValues = new TreeSet<>();
            for (String itemID : testSet.get(userID)) {
                pageRankValues.add(new Rating(itemID, hits.getVertexScore(itemID) + ""));
            }
            recommendationList.put(userID, pageRankValues);

        }

        return recommendationList;
    }
}
