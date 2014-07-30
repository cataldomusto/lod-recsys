package di.uniba.it.lodrecsys.graph.scorer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.utils.PropertiesCalculator;
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

    private Multimap<String, String> loadItemRepresentation(String itemResource) {
        Multimap<String, String> itemRepresentation = ArrayListMultimap.create();

        List<Statement> propStatement = propManager.getResourceProperties(itemResource);

        for (Statement stat : propStatement) {
            itemRepresentation.put(stat.getPredicate().toString(), stat.getObject().toString());
        }

        return itemRepresentation;

    }

    @Override
    public Double transform(String entityID) {


        // 40% of the weight evenly distributed among training items
        if (trainingPos.contains(entityID)) {
            return 0.4 / trainingPos.size();
        }

        // no additional weight for user or not liked items
        if (trainingNeg.contains(entityID) || entityID.startsWith("U:")) {
            return 0d;
        }


        // 60% * sim_score distributed among all the other items
        Multimap<String, String> currItemRepresentation = itemsRepresentationMap.get(entityID);

        return (currItemRepresentation != null) ?
                (0.6 * PropertiesCalculator.computeJaccard(userCentroid, currItemRepresentation)) / (totalNumberItems - (trainingNeg.size() + trainingPos.size())) :
                (0.6 / (totalNumberItems - (trainingNeg.size() + trainingPos.size())));


    }
}
