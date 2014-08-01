package di.uniba.it.lodrecsys.graph.scorer;

/**
 * Created by asuglia on 8/1/14.
 */

import com.google.common.collect.Multimap;
import di.uniba.it.lodrecsys.properties.JaccardSimilarityFunction;
import di.uniba.it.lodrecsys.properties.SimilarityFunction;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import org.apache.commons.collections15.Transformer;

import java.util.Map;
import java.util.Set;

/**
 * Created by asuglia on 7/30/14.
 */
public class ImprovedJaccardTransformer implements Transformer<String, Double> {
    private Set<String> trainingPos;
    private Set<String> trainingNeg;
    private int totalNumberItems;
    private Map<String, Multimap<String, String>> usersCentroid;
    private Map<String, Multimap<String, String>> itemsRepresentationMap;
    private Multimap<String, String> mainUserCentroid;
    private String currUserID;
    private int totalNumberOfNodes;
    private int totalNumberOfUsers;


    public ImprovedJaccardTransformer(String currUserID, Set<String> trainingPos, Set<String> trainingNeg, int totalNumberItems, int totalNumberOfNodes, Map<String, Multimap<String, String>> usersCentroid,
                                      Map<String, Multimap<String, String>> itemsRepresentationMap) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.totalNumberItems = totalNumberItems;
        this.usersCentroid = usersCentroid;
        this.totalNumberOfNodes = totalNumberOfNodes;
        this.itemsRepresentationMap = itemsRepresentationMap;
        this.mainUserCentroid = this.usersCentroid.get(currUserID);
        this.totalNumberOfUsers = this.usersCentroid.keySet().size();
        this.currUserID = currUserID;
    }

    @Override
    public Double transform(String entityID) {


        if (trainingPos.contains(entityID)) {
            return 0.80 / trainingPos.size();
        }

        // no additional weight for user or not liked items
        if (trainingNeg.contains(entityID)) {
            return 0d;
        }

        // jaccard score distributed among user
        if (entityID.startsWith("U:")) {
            String userID = entityID.substring(entityID.indexOf(":") + 1, entityID.length());

            if (this.currUserID.equals(userID))
                return 1d;

            Multimap<String, String> otherUserCentroid = usersCentroid.get(userID);

            if (otherUserCentroid == null)
                return 0d;

            SimilarityFunction function = new JaccardSimilarityFunction();

            double jaccardScore = function.compute(otherUserCentroid, mainUserCentroid);


            return (jaccardScore >= 0.5) ? 0.8 / totalNumberOfUsers : 0.2 / totalNumberOfUsers;


        }

        Multimap<String, String> currItemRepresentation = itemsRepresentationMap.get(entityID);
        SimilarityFunction function = new JaccardSimilarityFunction();

        if (currItemRepresentation != null) {
            double jaccardScore = function.compute(mainUserCentroid, currItemRepresentation);
            return (jaccardScore >= 0.5) ? 0.8 / (totalNumberItems - (trainingNeg.size() + trainingPos.size())) :
                    0.2 / (totalNumberItems - (trainingNeg.size() + trainingPos.size()));
        } else {
            return 0.2 / (totalNumberItems - (trainingNeg.size() + trainingPos.size()));
        }

    }
}

