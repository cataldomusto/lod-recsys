package di.uniba.it.lodrecsys.graph.scorer;

import org.apache.commons.collections15.Transformer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class which defines a simple method that is used in order
 * to distribute weights to the nodes.
 * In particular, 80% of the weights is distributed equally among
 * positively rated items. 20% is distributed among all the other items and
 * 0 is directly assigned to negatively rated items.
 */
public class SimpleVertexTransformer implements Transformer<String, Double> {

    private final Map<String, String> uriIdMap;
    private Set<String> trainingPos;
    private Set<String> trainingNeg;
    private int graphSize;
    private double massProb = 0.8;


    public SimpleVertexTransformer(Set<String> trainingPos, Set<String> trainingNeg, int graphSize, Map<String, String> uriIdMap) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.graphSize = graphSize;
        this.uriIdMap = uriIdMap;
    }

    public SimpleVertexTransformer(Set<String> trainingPos, Set<String> trainingNeg, int graphSize, double massProb) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.graphSize = graphSize;
        this.massProb = massProb;
        this.uriIdMap = new HashMap<>();
    }

    public SimpleVertexTransformer(Set<String> trainingPos, Set<String> trainingNeg, int graphSize, double massProb, Map<String, String> uriIdMap) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.graphSize = graphSize;
        this.uriIdMap = uriIdMap;
        this.massProb = massProb;
    }

    @Override
    public Double transform(String node) {
        String resourceID = uriIdMap.get(node),
                itemID = (resourceID == null) ? node : resourceID;

        boolean containsPos = trainingPos.contains(itemID),
                containsNeg = false;

        if (!containsPos)
            containsNeg = trainingNeg.contains(itemID);


        if (containsPos) {
            return massProb / (double) (trainingPos.size());
        } else if (containsNeg) {
            return 0d;
        } else {
            return (1 - massProb) / (double) (graphSize - (trainingPos.size() + trainingNeg.size()));
        }
    }

    public int getGraphSize() {
        return graphSize;
    }

    public void setGraphSize(int graphSize) {
        this.graphSize = graphSize;
    }

    public double getMassProb() {
        return massProb;
    }

    public void setMassProb(double massProb) {
        this.massProb = massProb;
    }

}
