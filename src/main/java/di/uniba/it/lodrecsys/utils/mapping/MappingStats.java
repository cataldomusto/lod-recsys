package di.uniba.it.lodrecsys.utils.mapping;

import com.sun.org.apache.xpath.internal.SourceTree;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.RITriple;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.IOException;
import java.util.*;

/**
 * Created by asuglia on 6/11/14.
 */
public class MappingStats {
    public static void main(String[] args) throws IOException {
        String ratingFile = "/home/asuglia/thesis/dataset/ml-100k/binarized/u%s.base",
                mappingFile = "mapping/item.mapping";
        int totalNumberOfUser = 943;
        List<MovieMapping> mappedItems = Utils.loadDBpediaMappingItems(mappingFile);
        List<String> unmappedItemID = getUnmappedItems(mappedItems);

        System.out.println("Unmapped items: " + unmappedItemID.size());

        for (int i = 1; i <= 5; i++) {
            String ratingFileName = String.format(ratingFile, i + "");
            Map<String, List<String>> ratingByItem = Utils.loadRatingForEachItem(ratingFileName);

            System.out.println("File: " + ratingFileName);
            calculateStatsForItem(ratingByItem, unmappedItemID, totalNumberOfUser);
            System.out.println();
        }
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


}
