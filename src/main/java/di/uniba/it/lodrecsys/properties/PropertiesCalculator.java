package di.uniba.it.lodrecsys.properties;

import com.google.common.collect.Multimap;
import di.uniba.it.lodrecsys.entity.Pair;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.*;

/**
 * Computes the representative element among a list according
 * to a specified similarity metric which is used to determine how much
 * distance there is between two different instances.
 */
public class PropertiesCalculator {
    private SimilarityFunction similarity;

    private PropertiesCalculator() {
    }

    private PropertiesCalculator(SimilarityFunction similarity) {
        this.similarity = similarity;
    }

    public static PropertiesCalculator create(Similarity simName) {
        switch (simName) {
            case JACCARD:
                return new PropertiesCalculator(new JaccardSimilarityFunction());
            default:
                return null;
        }
    }

    private static String getMinDistanceItem(Map<String, Double> distanceArray) {
        Pair<String, Double> currMin = new Pair<>("", Double.MAX_VALUE);

        for (Map.Entry<String, Double> pair : distanceArray.entrySet()) {
            if (pair.getValue() < currMin.value) {
                currMin.value = pair.getValue();
                currMin.key = pair.getKey();
            }
        }

        return currMin.key;
    }

    public SimilarityFunction getSimilarity() {
        return this.similarity;
    }

    public Multimap<String, String> computeCentroid(Map<String, Multimap<String, String>> mapper) {
        // Create the initial vector
        ICombinatoricsVector<String> initialVector = Factory.createVector(
                mapper.keySet());

        // Create a simple combination generator to generate 2-combinations of the initial vector
        Generator<String> gen = Factory.createSimpleCombinationGenerator(initialVector, 2);

        Map<ICombinatoricsVector<String>, Double> distanceValues = new LinkedHashMap<>();

        // Compute distance between each combination
        for (ICombinatoricsVector<String> combination : gen) {
            distanceValues.put(combination, 1 - (double) similarity.compute(mapper.get(combination.getValue(0)), mapper.get(combination.getValue(1))));
        }

        Set<String> itemsSet = mapper.keySet();
        Map<String, Double> distanceArray = new HashMap<>();

        Set<ICombinatoricsVector<String>> combinationVec = distanceValues.keySet();

        // for each item sum distance with it and the other
        for (String itemID : itemsSet) {
            double totSum = 0d;

            List<ICombinatoricsVector<String>> currentItemDistance = new ArrayList<>();

            for (ICombinatoricsVector<String> comb : combinationVec) {
                if (comb.contains(itemID)) {
                    currentItemDistance.add(comb);
                }
            }

            for (ICombinatoricsVector<String> comb : currentItemDistance) {
                totSum += distanceValues.get(comb);
            }

            distanceArray.put(itemID, totSum);


        }

        return mapper.get(getMinDistanceItem(distanceArray));


    }

}
