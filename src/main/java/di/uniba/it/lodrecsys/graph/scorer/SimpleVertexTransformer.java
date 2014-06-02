package di.uniba.it.lodrecsys.graph.scorer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

/**
 * @author pierpaolo
 */
public class SimpleVertexTransformer implements Transformer<String, Double> {

    private Set<String> trainingPos;

    private Set<String> trainingNeg;

    private int graphSize;

    private double massProb = 0.8;

    private final Map<String, Set<String>> uriIdMap;


    public SimpleVertexTransformer(Set<String> trainingPos, Set<String> trainingNeg, int graphSize, Map<String, Set<String>> uriIdMap) {
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

    public SimpleVertexTransformer(Set<String> trainingPos, Set<String> trainingNeg, int graphSize, Map<String, Set<String>> uriIdMap, double massProb) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.graphSize = graphSize;
        this.uriIdMap = uriIdMap;
        this.massProb = massProb;
    }

    @Override
    public Double transform(String node) {
        boolean containsPos = trainingPos.contains(node), containsNeg = false;
        if (!containsPos) {
            containsNeg = trainingNeg.contains(node);
        }

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
