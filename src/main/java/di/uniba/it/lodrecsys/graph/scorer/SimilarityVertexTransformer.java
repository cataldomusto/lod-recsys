package di.uniba.it.lodrecsys.graph.scorer;

import org.apache.commons.collections15.Transformer;

import java.util.Map;

/**
 * Class that defines a basic similarity weighting scheme.
 * Each nodes receive a normalized weight according to a similarity metric
 * defined.
 */
public class SimilarityVertexTransformer implements Transformer<String, Double> {
    private String currUserID;
    private Map<String, Map<String, Double>> simUserMap;

    public SimilarityVertexTransformer(String currUserID, Map<String, Map<String, Double>> simUserMap) {
        this.currUserID = currUserID;
        this.simUserMap = simUserMap;
    }

    @Override
    public Double transform(String entityID) {
        return simUserMap.get(currUserID).get(entityID);
    }
}
