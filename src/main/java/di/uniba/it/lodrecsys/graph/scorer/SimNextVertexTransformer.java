package di.uniba.it.lodrecsys.graph.scorer;

import org.apache.commons.collections15.Transformer;

import java.util.Map;
import java.util.Set;

/**
 * Class that models the heuristic used to distribute weights
 * according to a similarity metric defined.
 * In particular the whole weight is distributed among
 * all the items whose similarity with the current user,
 * is greater that the average computed similarity. Otherwise,
 * to those items is assigned 0, so they will be treated as irrelevant.
 */
public class SimNextVertexTransformer implements Transformer<String, Double> {
    private String currUserID;
    private Set<String> trainingPos;
    private Set<String> trainingNeg;
    private Map<String, Map<String, Double>> simUserMap;
    private double meanSimUser;
    private int underMean;
    private int overMean;

    public SimNextVertexTransformer(String currUserID, Set<String> trainingPos, Set<String> trainingNeg, Map<String, Map<String, Double>> simUserMap) {
        this.currUserID = currUserID;
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.simUserMap = simUserMap;
        this.meanSimUser = computeMeanSimilarityUser();
        computeGroupsNumber();
    }

    private double computeMeanSimilarityUser() {
        Map<String, Double> currUserSim = simUserMap.get(currUserID);
        double finalVal = 0d;
        int totEntities = currUserSim.size();

        for (String entityID : currUserSim.keySet()) {
            finalVal += currUserSim.get(entityID);
        }

        return finalVal / totEntities;
    }

    private void computeGroupsNumber() {
        this.overMean = 0;

        Map<String, Double> currUserSim = simUserMap.get(currUserID);
        int totEntities = currUserSim.size();

        for (String entityID : currUserSim.keySet()) {
            if (currUserSim.get(entityID) >= meanSimUser)
                overMean++;
        }

        underMean = totEntities - overMean;
    }

    @Override
    public Double transform(String entityID) {

        if (entityID.startsWith("I:")) {
            String itemID = entityID.substring(entityID.indexOf("I:") + 1, entityID.length());

            if (trainingNeg.contains(itemID)) {
                return 0d;
            }

        }

        return simUserMap.get(currUserID).get(entityID) >= meanSimUser ? (double) 1 / overMean : 0;
    }
}
