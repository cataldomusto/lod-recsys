package di.uniba.it.lodrecsys.graph.scorer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.properties.JaccardSimilarityFunction;
import di.uniba.it.lodrecsys.properties.PropertiesCalculator;
import di.uniba.it.lodrecsys.properties.Similarity;
import di.uniba.it.lodrecsys.properties.SimilarityFunction;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import org.apache.commons.collections15.Transformer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by asuglia on 7/30/14.
 */
public class JaccardVertexTransformer implements Transformer<String, Double> {
    private Set<String> trainingPos;
    private Set<String> trainingNeg;
    private int totalNumberItems;
    private Multimap<String, String> userCentroid;
    private PropertiesManager propManager;
    private Map<String, Multimap<String, String>> itemsRepresentationMap;

    public JaccardVertexTransformer(Set<String> trainingPos, Set<String> trainingNeg, int totalNumberItems, Multimap<String, String> userCentroid,
                                    PropertiesManager propManager, Map<String, Multimap<String, String>> itemsRepresentationMap) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.totalNumberItems = totalNumberItems;
        this.userCentroid = userCentroid;
        this.propManager = propManager;
        this.itemsRepresentationMap = itemsRepresentationMap;
    }

    @Override
    public Double transform(String entityID) {


        // 35% of the weight evenly distributed among training items
        if (trainingPos.contains(entityID)) {
            return 0.35 / trainingPos.size();
        }

        // no additional weight for user or not liked items
        if (trainingNeg.contains(entityID) || entityID.startsWith("U:")) {
            return 0d;
        }


        // 60% * sim_score distributed among all the other items
        Multimap<String, String> currItemRepresentation = itemsRepresentationMap.get(entityID);
        SimilarityFunction function = new JaccardSimilarityFunction();
        return (currItemRepresentation != null) ?
                (0.75 * function.compute(userCentroid, currItemRepresentation)) / (totalNumberItems - (trainingNeg.size() + trainingPos.size())) :
                (0.75 / (totalNumberItems - (trainingNeg.size() + trainingPos.size())));


    }
}
