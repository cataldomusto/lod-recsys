package di.uniba.it.lodrecsys.graph.scorer;

import org.apache.commons.collections15.Transformer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by asuglia on 6/1/14.
 */
public class UserVertexTransformer implements Transformer<String, Double> {
    private Set<String> trainingPos;

    private Set<String> trainingNeg;

    private int graphSize;

    private String currUserID;
    private double massProb = 0.8;


    public UserVertexTransformer(Set<String> trainingPos, Set<String> trainingNeg, int graphSize, double massProb, String currUserID) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.graphSize = graphSize;
        this.currUserID = currUserID;
        this.massProb = massProb;
    }


    @Override
    public Double transform(String node) {

        if (trainingNeg.contains(node))
            return 0d;

        if (node.equals(currUserID))
            return 0.8d / graphSize;

        return 02d / graphSize;
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
