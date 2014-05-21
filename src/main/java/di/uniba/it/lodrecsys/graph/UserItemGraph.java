package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

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

}
