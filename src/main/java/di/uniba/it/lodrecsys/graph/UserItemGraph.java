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

    public UserItemGraph(String trainingFileName) throws IOException {
        super(trainingFileName);
    }

    @Override
    public void generateGraph(String trainingFileName) throws IOException {
        Map<String, Set<Rating>> userRatings = Utils.loadRatingForEachUser(trainingFileName);

        for(String userID : userRatings.keySet()) {
            // for each rating in the user's rating set
            // selects only the positive training ratings (1)
            int edgeCounter = 0; // counts the number of edges from the current UserNode

            for(Rating rate :userRatings.get(userID)) {
                if(rate.getRating().equals("1")) {
                    recGraph.addEdge(userID + "-" + edgeCounter, userID, rate.getItemID());
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
            DataModel testModel = (DataModel) requestParam.params.get(0);
            int numRec = (int) requestParam.params.get(1); // number of recommendation

            PageRank<String, String> pageRank = new PageRank<>(this.recGraph, 0.15);

            pageRank.setMaxIterations(25);

            pageRank.evaluate();

            Set<Rating> pageRankValues = new TreeSet<>();
            int totElement = 0;

            for (LongPrimitiveIterator itemIter = testModel.getItemIDs(); itemIter.hasNext(); ) {
                String itemID = itemIter.nextLong() + "";
                pageRankValues.add(new Rating(itemID, pageRank.getVertexScore(itemID) + ""));
                totElement++;
                if (totElement == numRec)
                    break; // the method has got all the recommendation
            }

            // print recommendation for all users

            for (LongPrimitiveIterator userIter = testModel.getUserIDs(); userIter.hasNext(); ) {
                String userID = userIter.nextLong() + "";
                int i = 0;
                for (Rating rate : pageRankValues) {
                    String trecLine = userID + " Q0 " + rate.getItemID() + " " + i++ + " " + rate.getRating() + " EXP";
                    writer.write(trecLine);
                    writer.newLine();
                }

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
