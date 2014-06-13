package di.uniba.it.lodrecsys.utils.mapping;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import com.sun.org.apache.xpath.internal.SourceTree;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.RITriple;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by asuglia on 6/11/14.
 */
public class MappingStats {
    public static void main(String[] args) throws IOException {
//        String ratingFile = "/home/asuglia/thesis/dataset/ml-100k/binarized/u%s.base",
          String mappingFile = "mapping/item.mapping";
//        int totalNumberOfUser = 943;
//        List<MovieMapping> mappedItems = Utils.loadDBpediaMappingItems(mappingFile);
//        List<String> unmappedItemID = getUnmappedItems(mappedItems);
//
//        System.out.println("Unmapped items: " + unmappedItemID.size());
//
//        for (int i = 1; i <= 5; i++) {
//            String ratingFileName = String.format(ratingFile, i + "");
//            Map<String, List<String>> ratingByItem = Utils.loadRatingForEachItem(ratingFileName);
//
//            System.out.println("File: " + ratingFileName);
//            calculateStatsForItem(ratingByItem, unmappedItemID, totalNumberOfUser);
//            System.out.println();
//        }
        System.setProperty("proxyHost", "wproxy.ict.uniba.it");
        System.setProperty("proxyPort", "80");
        System.setProperty("proxyUser", "a.suglia2");
        System.setProperty("proxyPass", "22.kyfuln");
        List<String> mappedURI = getDBpediaEntities(mappingFile);
        Multiset<String> resourceCounter = countResourceFreq(mappedURI);

        for(String propURI : resourceCounter.elementSet()) {
            System.out.println("URI: " + propURI + " - FREQ: " + resourceCounter.count(propURI));

        }



    }

    private static Multiset<String> countResourceFreq(List<String> mappedURI) {
        Multiset<String> resourceCounter = HashMultiset.create();

        SPARQLClient sparqlClient = new SPARQLClient();

        for(String uri : mappedURI) {
            Set<String> distinctProp = sparqlClient.getURIProperties(uri);
            for(String prop : distinctProp)
                resourceCounter.add(prop);

        }

        return resourceCounter;
    }

    private static List<String> getUnmappedItems(List<MovieMapping> mappings) {
        List<String> unmapped = new ArrayList<>();
        for (MovieMapping item : mappings) {
            if (item.getDbpediaURI().equals("null")) {
                unmapped.add(item.getItemID());
            }

        }

        return unmapped;

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


    private static List<String> getDBpediaEntities(String dbpediaMappingFile) throws IOException {
        BufferedReader reader = null;
        List<String> mappedItemsList = new ArrayList<>();
        try{
            reader = new BufferedReader(new FileReader(dbpediaMappingFile));


            while(reader.ready()) {
                String[] splittedLine = reader.readLine().split("\t");

                if(!splittedLine[2].equals("null"))
                    mappedItemsList.add(splittedLine[2]);

            }

            return mappedItemsList;

        } catch(IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(reader != null)
                reader.close();

        }


    }


}
