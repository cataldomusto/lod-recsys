package di.uniba.it.lodrecsys.utils;

import di.uniba.it.lodrecsys.entity.RITriple;
import di.uniba.it.lodrecsys.entity.Rating;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by asuglia on 5/5/14.
 */
public class PredictionFileConverter {
    private static List<RITriple> getScore(String line, String userId, Set<String> testSet) {
        List<RITriple> r = new ArrayList<>();
        line = line.substring(1, line.length() - 1);
        String[] split = line.split(",");
        for (String v : split) {
            String[] idScore = v.split(":");
            if (testSet.contains(idScore[0])) {
                r.add(new RITriple(idScore[0], userId, Float.parseFloat(idScore[1])));
            }
        }
        Collections.sort(r);
        return r;
    }


    private static Set<Rating> getRatingsSet(String[] ratings) {
        Set<Rating> ratingSet = new TreeSet<>();

        for (String rating : ratings) {
            String splitted[] = rating.split(":");
            if (splitted[0].startsWith("[")) {
                splitted[0] = splitted[0].substring(1, splitted[0].length());
            }

            if (splitted[1].endsWith("]")) {
                splitted[1] = splitted[1].substring(0, splitted[1].length() - 1);
            }
            ratingSet.add(new Rating(splitted[0], splitted[1]));
        }

        return ratingSet;

    }

    private static String ratingSetFormatter(Set<Rating> ratingSet) {
        StringBuilder ratingString = new StringBuilder("");
        int contItem = 0, numRating = ratingSet.size();
        ratingString.append("[");

        for (Rating rate : ratingSet) {

            ratingString.append(rate.getItemID());
            ratingString.append(":");
            ratingString.append(rate.getRating());
            contItem++;
            if (contItem != numRating) {
                ratingString.append(",");
            }

        }

        ratingString.append("]");

        return ratingString.toString();
    }

    /**
     * test_file MyMediaLite_prediction out_file
     */
    public static void fixPredictionFile(String testSetFile, String predictionFile, String newPredictionFile) throws IOException {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            Map<String, List<Rating>> testSet = Utils.loadRatingForEachUser(testSetFile);
            System.out.println("Users to evaluate: " + testSet.size());
            reader = new BufferedReader(new FileReader(predictionFile));
            writer = new BufferedWriter(new FileWriter(newPredictionFile));

            while (reader.ready()) {
                String line = reader.readLine();
                String[] lineSplitted = line.split("\t");
                String userID = lineSplitted[0];
                Set<Rating> predRatings = getRatingsSet(lineSplitted[1].split(","));
                int i = 0;

                List<Rating> testSetRatingsForUser = testSet.get(userID);
                if (testSetRatingsForUser != null) {
                    Set<Rating> newPredRatings = new TreeSet<>();

                    for (Rating rate : predRatings) {
                        if (isPresentItem(testSetRatingsForUser, rate.getItemID())) {
                            newPredRatings.add(rate);
                        }
                    }
                    if (newPredRatings.size() != 0) {
                        String resString = userID + "\t" + ratingSetFormatter(newPredRatings);
                        writer.write(resString);
                        writer.newLine();
                    }
                }

            }

        } catch (IOException ex) {
            Logger.getLogger(PredictionFileConverter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            assert reader != null;
            assert writer != null;

            reader.close();
            writer.close();

        }
    }

    private static boolean isPresentItem(List<Rating> ratingList, String itemID) {
        for (Rating rate : ratingList)
            if (rate.getItemID().equals(itemID))
                return true;

        return false;

    }
}
