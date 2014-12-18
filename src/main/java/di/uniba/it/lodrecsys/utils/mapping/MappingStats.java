package di.uniba.it.lodrecsys.utils.mapping;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.IOException;
import java.util.*;

/**
 * Defines some method used in order to understand which elements
 * belong to the dataset and their frequency
 */
public class MappingStats {
    public static void main(String[] args) throws IOException {
        String ratingFile = "/home/asuglia/thesis/dataset/ml-100k/binarized/u%s.base",
                mappingFile = "mapping/item.mapping";
        int totalNumberOfUser = 943;

        List<MovieMapping> allItems = Utils.loadDBpediaMappingItems(mappingFile);
        List<MovieMapping> unmappedItems = getUnmappedItems(allItems);

        for (MovieMapping movie : unmappedItems)
            System.out.println(movie);
    }

    private static void printResourceFreq(String mappingFile) throws IOException {

        List<String> mappedItems = Utils.getDBpediaEntities(mappingFile);

        Multiset<String> occurrences = countResourceFreq(mappedItems);

        for (String uri : occurrences.elementSet()) {
            System.out.println("DBPEDIA PROP: " + uri + " FREQ: " + occurrences.count(uri));

        }
    }


    private static Multiset<String> countResourceFreq(List<String> mappedURI) {
        Multiset<String> resourceCounter = HashMultiset.create();

        SPARQLClient sparqlClient = new SPARQLClient();

        for (String uri : mappedURI) {
            Set<String> distinctProp = sparqlClient.getURIProperties(uri);
            for (String prop : distinctProp)
                resourceCounter.add(prop);

        }

        return resourceCounter;
    }

    private static List<MovieMapping> getUnmappedItems(List<MovieMapping> mappings) {
        List<MovieMapping> unmapped = new ArrayList<>();
        for (MovieMapping item : mappings) {
            if (item.getDbpediaURI().equals("null")) {
                unmapped.add(item);
            }

        }

        return unmapped;

    }

    private static int getTotalNumberOfRatings(Map<String, List<String>> ratingsByItem, List<String> unmappedItems) {
        int total = 0;
        for (String item : unmappedItems) {
            List<String> userList = ratingsByItem.get(item);
            total += userList != null ? userList.size() : 0;
        }

        return total;

    }

    private static int totalNumberOfUsersForSplit(Map<String, List<String>> ratingByItem) {
        Set<String> usersSet = new TreeSet<>();

        for (String itemID : ratingByItem.keySet()) {
            usersSet.addAll(ratingByItem.get(itemID));
        }

        return usersSet.size();
    }


    private static void calculateStatsForItem(Map<String, List<String>> ratingByItem, List<String> notMappedItem, int totalNumberUser) {
        for (String itemID : notMappedItem) {
            List<String> userList = ratingByItem.get(itemID);
            if (userList != null)
                System.out.println("Average number of ratings for item " + itemID + ": " + (double) userList.size() / (double) totalNumberUser);
            else
                System.out.println("No ratings for item " + itemID);

        }


    }


}
