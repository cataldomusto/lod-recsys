package di.uniba.it.lodrecsys.utils;

import di.uniba.it.lodrecsys.entity.Pair;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.*;

/**
 * Created by asuglia on 7/6/14.
 */
public class PropertiesCalculator {
    public static double computeJaccard(List<String> first, List<String> second) {
        double totInter = 0, totUnion = 0;

        for (int i = 0; i < first.size(); i++) {
            if (first.get(i).equals(second.get(i)))
                totInter++;
        }


        return totInter / (first.size() + second.size() - totInter);

    }


    public static List<String> computeCentroid(Map<String, List<String>> mapper) {
        // Create the initial vector
        ICombinatoricsVector<String> initialVector = Factory.createVector(
                mapper.keySet());

        // Create a simple combination generator to generate 2-combinations of the initial vector
        Generator<String> gen = Factory.createSimpleCombinationGenerator(initialVector, 2);

        Map<ICombinatoricsVector<String>, Double> distanceValues = new LinkedHashMap<>();

        // Compute distance between each combination
        for (ICombinatoricsVector<String> combination : gen) {
            distanceValues.put(combination, 1 - computeJaccard(mapper.get(combination.getValue(0)), mapper.get(combination.getValue(1))));
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


    public static void main(String[] args) {
        Map<String, List<String>> mapper = new HashMap<>();

        List<String> first = new ArrayList<>(),
                sec = new ArrayList<>(),
                third = new ArrayList<>(),
                forth = new ArrayList<>();

        first.add("A1");
        first.add("B1");
        first.add("C2");


        sec.add("A1");
        sec.add("B2");
        sec.add("C2");

        third.add("A2");
        third.add("B1");
        third.add("C2");


        forth.add("A2");
        forth.add("B1");
        forth.add("C1");


        mapper.put("I1", first);
        mapper.put("I2", sec);
        mapper.put("I3", third);
        mapper.put("I4", forth);


        List<String> centroid = computeCentroid(mapper);

        System.out.println(centroid);


    }

}
