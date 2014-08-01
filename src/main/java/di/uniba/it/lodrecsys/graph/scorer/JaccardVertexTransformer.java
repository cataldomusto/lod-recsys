package di.uniba.it.lodrecsys.graph.scorer;

import com.google.common.collect.Multimap;
import di.uniba.it.lodrecsys.properties.JaccardSimilarityFunction;
import di.uniba.it.lodrecsys.properties.SimilarityFunction;
import org.apache.commons.collections15.Transformer;

import java.util.Map;
import java.util.Set;

/**
 * Created by asuglia on 7/30/14.
 */
public class JaccardVertexTransformer implements Transformer<String, Double> {
    private Set<String> trainingPos;
    private Set<String> trainingNeg;
    private int totalNumberNodes;
    private Multimap<String, String> currUserCentroid;
    private Map<String, Multimap<String, String>> itemsRepresentationMap;
    private Map<String, Multimap<String, String>> usersCentroid;

    public JaccardVertexTransformer(Set<String> trainingPos, Set<String> trainingNeg, int totalNumberNodes, Multimap<String, String> currUserCentroid,
                                    Map<String, Multimap<String, String>> itemsRepresentationMap, Map<String, Multimap<String, String>> usersCentroid) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.totalNumberNodes = totalNumberNodes;
        this.currUserCentroid = currUserCentroid;
        this.itemsRepresentationMap = itemsRepresentationMap;
        this.usersCentroid = usersCentroid;
    }

    @Override
    public Double transform(String entityID) {
        Double finalScore;

        // entityID is a item
        if (!entityID.startsWith("U:")) {
            if (trainingNeg.contains(entityID)) {
                finalScore = 0d;
            } else {
                Multimap<String, String> itemVector = itemsRepresentationMap.get(entityID);
                // we got a LOD representation for the item
                if (itemVector != null) {
                    SimilarityFunction simFunction = new JaccardSimilarityFunction();
                    finalScore = simFunction.compute(itemVector, currUserCentroid);

                } else { // no LOD features for the item
                    finalScore = 0.2 / totalNumberNodes;
                }
            }
        } else { // entity is a user
            String userID = entityID.substring(entityID.indexOf("U:") + 1, entityID.length());

            Multimap<String, String> userVector = usersCentroid.get(userID);
            // we got a LOD representation for the item
            if (userVector != null) {
                SimilarityFunction simFunction = new JaccardSimilarityFunction();
                finalScore = simFunction.compute(userVector, currUserCentroid);
            } else { // no LOD features for the item
                finalScore = 0.2 / totalNumberNodes;
            }
        }

        return finalScore;
    }
}
