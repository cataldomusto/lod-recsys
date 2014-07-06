package di.uniba.it.lodrecsys.graph.scorer;

import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.utils.PropertiesCalculator;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import org.apache.commons.collections15.Transformer;

import java.util.*;

/**
 * Created by asuglia on 7/1/14.
 */
public class WeightedVertexTransformer implements Transformer<String, Double> {
    private List<String> userProfile;
    Map<String, String> mappedItems;

    private int graphSize;

    private PropertiesManager manager;
    private double massProb = 0.8;

    public WeightedVertexTransformer(PropertiesManager manager, Set<String> trainingPos, Set<String> trainingNeg,
                                     Map<String, String> mappedItems, int graphSize) {
        this.graphSize = graphSize;
        this.manager = manager;
        this.mappedItems = mappedItems;
        generateUserProfile(trainingPos, trainingNeg, mappedItems);
    }


    private void generateUserProfile(Set<String> trainingPos, Set<String> trainingNeg, Map<String, String> mappedItems) {
        Map<String, List<String>> propMapper = new HashMap<>();


        // Take all the properties from the user's positive items
        for (String itemID : trainingPos) {
            propMapper.put(itemID, generateItemPropList(itemID));
        }

        this.userProfile = PropertiesCalculator.computeCentroid(propMapper);


    }

    private List<String> generateItemPropList(String itemID) {
        List<String> propList = new ArrayList<>();
        for (Statement stat : manager.getResourceProperties(mappedItems.get(itemID))) {
            propList.add(stat.getObject().toString());
        }

        return propList;
    }


    @Override
    public Double transform(String node) {
        // User node
        if (node.startsWith("U:")) {
            //return computeSimilarity(getUserProfile(node), currUserProfile) / sumSimilarity(userI, currUserProfile)

        } else if (node.startsWith("I:")) { // item node
            //return computeSimilarity(generateItemPropList(node), currUserProfile) / sumSimilarity(itemI, currUserProfile)
        } else { // other (propr, tagme, ...)
            // some kind of value distributed among propr, tagme and other
            // |other| = (graphsize - (user+item))
        }

        return 0d;
    }

}
