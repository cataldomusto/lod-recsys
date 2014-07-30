package di.uniba.it.lodrecsys.utils;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by asuglia on 7/6/14.
 */
public class PropertiesCalculator {
    public static double computeJaccard(Multimap<String, String> first, Multimap<String, String> second) {
        Set<String> firstElements = new TreeSet<>(),
                secElements = new TreeSet<>();

        for (String firstKey : first.keySet()) {
            for (String propValue : first.get(firstKey)) {
                firstElements.add(firstKey + ":" + propValue);
            }
        }

        for (String secKey : second.keySet()) {
            for (String propValue : second.get(secKey)) {
                secElements.add(secKey + ":" + propValue);
            }
        }


        return (double) Sets.intersection(firstElements, secElements).size() / (double) Sets.union(firstElements, secElements).size();

    }


    public static Multimap<String, String> computeCentroid(Map<String, Multimap<String, String>> mapper) {
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


    public static void main(String[] args) throws IOException {
        PropertiesManager manager = new PropertiesManager("/home/asuglia/thesis/content_lodrecsys/movielens/stored_prop");
        List<MovieMapping> mappings = Utils.loadDBpediaMappedItems("mapping/item.mapping");
        Collection<String> properties = loadPropertiesURI("mapping/choosen_prop.txt");

        Model model = manager.datasetModel;
        Map<String, Integer> mapper = new HashMap<>();

        for (MovieMapping movie : mappings) {
            Resource currMovieResource = model.createResource(movie.getDbpediaURI());

            for (String prop : properties) {
                int cont;

                NodeIterator it = model.listObjectsOfProperty(currMovieResource, model.createProperty(prop));

                for (cont = 0; it.hasNext(); cont++)
                    it.next();

                mapper.put(prop, mapper.getOrDefault(prop, 0) + cont);
                //System.out.println("Movie '" + movie.getItemID() + "' has " + cont + " objects for " + prop);
            }

            //System.out.println();
        }

        double totEntities = mappings.size();

        for (String prop : properties) {
            System.out.println("Average for prop: " + prop + " " + (double) mapper.get(prop) / totEntities);
        }

        System.out.println(formatPropertiesList(properties, true));


    }

    private static String formatPropertiesList(Collection<String> specificProp, boolean areURI) {
        String formattedProperties = "";

        for (String prop : specificProp)
            formattedProperties += (areURI) ? "<" + prop + ">\n" : prop + "\n";

        return formattedProperties;

    }

    private static Collection<String> loadPropertiesURI(String fileName) {
        BufferedReader reader = null;
        Collection<String> listProp = new ArrayList<>();

        try {
            reader = new BufferedReader(new FileReader(fileName));


            while (reader.ready()) {
                listProp.add(reader.readLine());

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return listProp;

    }

}
