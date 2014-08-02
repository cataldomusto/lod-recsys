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
    private String currUserID;
    private Set<String> trainingPos;
    private Set<String> trainingNeg;
    private Map<String, Map<String, Double>> simUserMap;

    public JaccardVertexTransformer(String currUserID, Set<String> trainingPos, Set<String> trainingNeg, Map<String, Map<String, Double>> simUserMap) {
        this.currUserID = currUserID;
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.simUserMap = simUserMap;
    }

    @Override
    public Double transform(String entityID) {

        if (entityID.startsWith("I:")) {
            String itemID = entityID.substring(entityID.indexOf("I:") + 1, entityID.length());

            if (trainingNeg.contains(itemID)) {
                return 0d;
            }

        }

        return simUserMap.get(currUserID).get(entityID);
    }
}
