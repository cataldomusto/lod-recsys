package di.uniba.it.lodrecsys.utils.mapping;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.*;
import java.util.*;

/**
 * Defines some method used in order to understand which elements
 * belong to the dataset and their frequency
 */
public class MappingStats {
    public static void main(String[] args) throws IOException {
        LoadProperties.init(args[0]);
        String mappingFile = LoadProperties.DBPEDIAMAPPING + args[1];

        List<MovieMapping> allItems = Utils.loadDBpediaMappingItems(mappingFile);
        List<MovieMapping> unmappedItems = getUnmappedItems(allItems);

        for (MovieMapping movie : unmappedItems)
            System.out.println(movie);
        printResourceFreq(mappingFile, args[1]);
    }

    private static void printResourceFreq(String mappingFile, String arg) throws IOException {

        List<String> mappedItems = Utils.getDBpediaEntities(mappingFile);

        Multiset<String> occurrences = countResourceFreq(mappedItems, arg);

        if (new File(LoadProperties.MAPPINGPATH + "/stat" + arg).exists()) {
            new File(LoadProperties.MAPPINGPATH + "/stat" + arg).delete();
        }

        for (String uri : occurrences.elementSet()) {
//            savefileLog("DBPEDIA PROP: " + uri + " FREQ: " + occurrences.count(uri));
            savefileLog(occurrences.count(uri) + " " + uri, arg);
        }
    }

    private static void savefileLog(String s, String arg) {
        String dir = LoadProperties.MAPPINGPATH;
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir + "/stat" + arg, true)))) {
            out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(s);
    }

    private static Multiset<String> countResourceFreq(List<String> mappedURI, String arg) {
        Multiset<String> resourceCounter = HashMultiset.create();

        SPARQLClient sparqlClient = new SPARQLClient();

        String mappingFile = LoadProperties.DBPEDIAMAPPING + arg;

        int count = 1;
        for (String uri : mappedURI) {
            System.out.println(new Date() + " [INFO " + mappingFile + "] Resource " + count + " of " + mappedURI.size());
            Set<String> distinctProp = sparqlClient.getURIProperties(uri);
            for (String prop : distinctProp)
                resourceCounter.add(prop);
            count++;
        }
        System.out.println("\n " + mappingFile + " finished.\n---------------------------------\n\n");
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
