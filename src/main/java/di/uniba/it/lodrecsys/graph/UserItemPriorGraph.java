package di.uniba.it.lodrecsys.graph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.utils.Utils;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by asuglia on 5/26/14.
 */
public class UserItemPriorGraph extends RecGraph {
    private ArrayListMultimap<String, Set<String>> posNegRatingsForUsers;

    public UserItemPriorGraph(String trainingFileName) {
        try {
            generateGraph(trainingFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void generateGraph(String trainingFileName) throws IOException {
        posNegRatingsForUsers = Utils.loadPosNegRatingForEachUser(trainingFileName);

        for (String userID : posNegRatingsForUsers.keySet()) {
            int edgeCounter = 0;
            for (String posItemID : posNegRatingsForUsers.get(userID).get(0)) {
                recGraph.addEdge(userID + "-" + edgeCounter, userID, posItemID);
                edgeCounter++;

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

            // print recommendation for all users

            for (LongPrimitiveIterator userIter = testModel.getUserIDs(); userIter.hasNext(); ) {
                long userIDLong = userIter.nextLong();
                String userID = userIDLong + "";
                int i = 0;
                LongPrimitiveIterator itemIter = testModel.getItemIDsFromUser(userIDLong).iterator();
                List<Set<String>> posNegativeRatings = posNegRatingsForUsers.get(userID);
                Set<Rating> currUserRatings = profileUser(posNegativeRatings.get(0), posNegativeRatings.get(1), itemIter, numRec);
                serializeRatings(userID, currUserRatings, writer);
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


    private Set<Rating> profileUser(Set<String> trainingPos, Set<String> trainingNeg, LongPrimitiveIterator testIterator, int numRec) {
        Set<Rating> recommendation = new TreeSet<>();

        VertexTransformer transformer = new VertexTransformer(trainingPos, trainingNeg, this.recGraph.getVertexCount());
        PageRankWithPriors<String, String> priors = new PageRankWithPriors<>(this.recGraph, transformer, 0.15);

        priors.setMaxIterations(25);
        priors.evaluate();

        int i = 0;
        while (testIterator.hasNext()) {
            String currItemID = "" + testIterator.nextLong();
            try {
                recommendation.add(new Rating(currItemID, priors.getVertexScore(currItemID) + ""));
            } catch (IllegalArgumentException exp) {
                recommendation.add(new Rating(currItemID, "0"));
            }
            i++;
            if (i == numRec)
                break;
        }


        return recommendation;
    }


}
