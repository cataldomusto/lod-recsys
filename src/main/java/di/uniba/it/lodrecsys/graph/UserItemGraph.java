package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.utils.Utils;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.Graph;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by asuglia on 5/21/14.
 */
public class UserItemGraph extends RecGraph {
    private Map<String, Set<Rating>> trainingSet;
    private Map<String, Set<String>> testSet;

    public UserItemGraph(String trainingFileName, String testFile) throws IOException {
        super(trainingFileName, testFile);
    }

    @Override
    public void generateGraph(String trainingFileName, String testFile) throws IOException {
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
    public void runPageRank(String resultFile, RequestStruct requestParam) throws IOException, TasteException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(resultFile));
            int numRec = (int) requestParam.params.get(0); // number of recommendation

            PageRank<String, String> pageRank = new PageRank<>(this.recGraph, 0.15);

            pageRank.setMaxIterations(25);

            pageRank.evaluate();

            Set<Rating> pageRankValues = new TreeSet<>();

            // print recommendation for all users

            for (String userID : testSet.keySet()) {
                int totElement = 0;
                for (String itemID : testSet.get(userID)) {
                    pageRankValues.add(new Rating(itemID, pageRank.getVertexScore(itemID) + ""));
                    if (++totElement == numRec)
                        break; // the method has got all the recommendation
                }

                serializeRatings(userID, pageRankValues, writer);
                pageRankValues.clear();
            }


        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (writer != null) {
                writer.close();
            }

        }
    }
}
