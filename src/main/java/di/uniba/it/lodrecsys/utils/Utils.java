package di.uniba.it.lodrecsys.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import di.uniba.it.lodrecsys.entity.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pierpaolo
 */
public class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class.getName());

    public static Map<String, Set<String>> loadUriIdMapping(File mappingFile, boolean findDuplicate) throws IOException {
        //load mapping between book URI and id
        Map<String, Set<String>> uriIdMap = new HashMap<String, Set<String>>();
        BufferedReader reader = new BufferedReader(new FileReader(mappingFile));
        reader.readLine();
        while (reader.ready()) {
            String line = reader.readLine();
            String[] split = line.split("\t");
            String id = split[0];
            String uri = split[2];
            if (findDuplicate && uriIdMap.containsKey(uri)) {
                logger.log(Level.WARNING, "Duplicate uri {0}", line);
            }
            Set<String> set = uriIdMap.get(uri);
            if (set == null) {
                set = new HashSet<String>();
                uriIdMap.put(uri, set);
            }
            set.add(id);
        }
        reader.close();
        logger.log(Level.INFO, "Loaded {0} uri->id mapping", uriIdMap.size());
        return uriIdMap;
    }

    public static Map<String, Set<String>> loadUriIdWithTitleMapping(File mappingFile, boolean findDuplicate) throws IOException {
        //load mapping between book URI and id
        Map<String, Set<String>> uriIdMap = new HashMap<String, Set<String>>();
        BufferedReader reader = new BufferedReader(new FileReader(mappingFile));
        reader.readLine();
        while (reader.ready()) {
            String line = reader.readLine();
            String[] split = line.split("\t");
            String id = split[0];
            String title = split[1];
            String uri = split[2];
            if (findDuplicate && uriIdMap.containsKey(uri)) {
                logger.log(Level.WARNING, "Duplicate uri {0}", line);
            }
            Set<String> set = uriIdMap.get(uri);
            if (set == null) {
                set = new HashSet<String>();
                uriIdMap.put(uri, set);
            }
            set.add(id + "_" + title);
        }
        reader.close();
        logger.log(Level.INFO, "Loaded {0} uri->id mapping", uriIdMap.size());
        return uriIdMap;
    }

    public static Map<String, String> loadIdUriMapping(File mappingFile) throws IOException {
        //load mapping between book id and URI
        Map<String, String> uriIdMap = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(new FileReader(mappingFile));
        reader.readLine();
        while (reader.ready()) {
            String[] split = reader.readLine().split("\t");
            String id = split[0];
            String uri = split[2];
            uriIdMap.put(id, uri);
        }
        reader.close();
        return uriIdMap;
    }

    public static List<ItemScore> loadMostPopular(File file, boolean skipHeader, int n) throws IOException {
        List<ItemScore> mostPopular = new ArrayList<ItemScore>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        if (skipHeader && reader.ready()) {
            reader.readLine();
        }
        String line;
        String[] split;
        while (reader.ready()) {
            line = reader.readLine();
            split = line.split("\t");
            mostPopular.add(new ItemScore(split[0], Float.parseFloat(split[1])));
        }
        reader.close();
        Collections.sort(mostPopular);
        if (mostPopular.size() > n) {
            mostPopular = mostPopular.subList(0, n);
        }
        return mostPopular;
    }

    public static List<ItemScore> loadMostRatioPopular(File file, boolean skipHeader, int n) throws IOException {
        List<ItemScore> mostPopular = new ArrayList<ItemScore>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        if (skipHeader && reader.ready()) {
            reader.readLine();
        }
        String line;
        String[] split;
        while (reader.ready()) {
            line = reader.readLine();
            split = line.split("\t");
            mostPopular.add(new ItemScore(split[0], (Float.parseFloat(split[1]) + 1) / (Float.parseFloat(split[2]) + 1)));
        }
        reader.close();
        Collections.sort(mostPopular);
        if (mostPopular.size() > n) {
            mostPopular = mostPopular.subList(0, n);
        }
        return mostPopular;
    }

    public static List<ItemScore> loadMostPopularPositiveProb(File file, boolean skipHeader, int n) throws IOException {
        List<ItemScore> mostPopular = new ArrayList<ItemScore>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        if (skipHeader && reader.ready()) {
            reader.readLine();
        }
        String line;
        String[] split;
        while (reader.ready()) {
            line = reader.readLine();
            split = line.split("\t");
            mostPopular.add(new ItemScore(split[0], Float.parseFloat(split[1]) / (Float.parseFloat(split[1]) + Float.parseFloat(split[2]))));
        }
        reader.close();
        Collections.sort(mostPopular);
        if (mostPopular.size() > n) {
            mostPopular = mostPopular.subList(0, n);
        }
        return mostPopular;
    }

    public static List<RITriple> loadRating(File file, boolean skipHeader) throws IOException {
        List<RITriple> list = new ArrayList<RITriple>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        if (skipHeader && reader.ready()) {
            reader.readLine();
        }
        String line;
        String[] split;
        while (reader.ready()) {
            line = reader.readLine();
            split = line.split("\t");
            RITriple triple = RITriple.create(split);
            list.add(triple);
        }
        reader.close();
        return list;
    }

    public static List<RITriple> loadRatingByUser(File file, String userId, boolean skipHeader) throws IOException {
        List<RITriple> list = new ArrayList<RITriple>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        if (skipHeader && reader.ready()) {
            reader.readLine();
        }
        String line;
        String[] split;
        while (reader.ready()) {
            line = reader.readLine();
            split = line.split("\t");
            if (split[0].equals(userId)) {
                RITriple triple = RITriple.create(split);
                list.add(triple);
            }
        }
        reader.close();
        return list;
    }


    public static int serializeMappingList(List<MovieMapping> movieList, String movieMappingFile) throws IOException {
        BufferedWriter writer = null;

        try {
            int numPrinted = 0;
            writer = new BufferedWriter(new FileWriter(movieMappingFile, true));
            for (MovieMapping movie : movieList) {
                String movieLine = movie.getItemID() + "\t" + movie.getName() + "\t" +
                        movie.getDbpediaURI() + "\t" + movie.getYear();
                numPrinted++;
                writer.write(movieLine);
                writer.newLine();

            }

            return numPrinted;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(writer != null) {
                writer.close();
            }

        }


    }

    public static List<MovieMapping> getMovieTitles(String movieTitleFile) throws IOException {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(movieTitleFile));
            List<MovieMapping> mappingEntities = new ArrayList<>();

            String currLine;
            while ((currLine = reader.readLine()) != null) {
                String[] splittedParts = currLine.split("\\|\\|");
                String[] splitted = splittedParts[0].split("\\|");
                // First three field are the relevant one
                // splitted[0] movieID
                // splitted[1] movieTitle
                // splitted[2] movieDate

                // gets only the Year


                if (splitted.length == 3) {
                    String movieYear = splitted[1].substring(splitted[1].indexOf("(")+1, splitted[1].indexOf(")"));
                    mappingEntities.add(new MovieMapping(splitted[0], null, splitted[1], movieYear));
                }
            }

            return mappingEntities;

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            throw ex;
        } finally {
            if (reader != null)
                reader.close();

        }


    }


    public static List<RITriple> loadRatingByItem(File file, String itemId, boolean skipHeader) throws IOException {
        List<RITriple> list = new ArrayList<RITriple>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        if (skipHeader && reader.ready()) {
            reader.readLine();
        }
        String line;
        String[] split;
        while (reader.ready()) {
            line = reader.readLine();
            split = line.split("\t");
            if (split[1].equals(itemId)) {
                RITriple triple = RITriple.create(split);
                list.add(triple);
            }
        }
        reader.close();
        return list;
    }

    public static ArrayListMultimap<String, Set<String>> loadPosNegRatingForEachUser(String ratingFile) throws IOException {
        ArrayListMultimap<String, Set<String>> ratingsMap = ArrayListMultimap.create();
        Set<String> posRatingSet, negRatingSet;

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(ratingFile));

            while (reader.ready()) {
                String currLine = reader.readLine();
                String[] splittedLine = currLine.split("\t"); //USER_ID\tITEM_ID\tBIN_RATE
                List<Set<String>> posNegRatings = ratingsMap.get(splittedLine[0]);

                // Retrieve for the current user the old lists
                if (posNegRatings.size() != 0) {
                    posRatingSet = posNegRatings.get(0);
                    negRatingSet = posNegRatings.get(1);
                } else {
                    // if hasn't been associated to the current user pos-neg ratings
                    // the ArrayListMultimap already generates an entry in the multimap
                    // Create the new set for the rated items
                    posRatingSet = new TreeSet<>();
                    negRatingSet = new TreeSet<>();
                    // now add the set to the current user rating list
                    posNegRatings.add(posRatingSet);
                    posNegRatings.add(negRatingSet);
                }


                // add a positive item id to the positive items
                if (splittedLine[2].equals("1")) {
                    posRatingSet.add(splittedLine[1]);
                } else { // add a positive item id to the negative items
                    negRatingSet.add(splittedLine[1]);
                }


            }

        } catch (IOException e) {
            throw e;
        } finally {
            assert reader != null;
            reader.close();

        }


        return ratingsMap;
    }


    public static Map<String, Set<Rating>> loadRatingForEachUser(String ratingFile) throws IOException {
        Map<String, Set<Rating>> ratingsMap = new HashMap<>();

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(ratingFile));

            while (reader.ready()) {
                String currLine = reader.readLine();
                String[] splittedLine = currLine.split("\t"); //USER_ID\tITEM_ID\tBIN_RATE
                Set<Rating> ratings = ratingsMap.get(splittedLine[0]);
                if (ratings == null) {
                    // start to fill ratings for the current user
                    ratings = new TreeSet<>();

                }

                ratings.add(new Rating(splittedLine[1], splittedLine[2]));
                ratingsMap.put(splittedLine[0], ratings);

            }

        } catch (IOException e) {
            throw e;
        } finally {
            assert reader != null;
            reader.close();

        }


        return ratingsMap;
    }


    public static Map<String, Set<String>> loadRatedItems(File file, boolean skipHeader) throws IOException {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        if (skipHeader && reader.ready()) {
            reader.readLine();
        }
        String line;
        String[] split;
        while (reader.ready()) {
            line = reader.readLine();
            split = line.split("\t");
            Set<String> set = map.get(split[0]);
            if (set == null) {
                set = new HashSet<String>();
                map.put(split[0], set);
            }
            set.add(split[1]);
        }
        reader.close();
        return map;
    }

    public static Set<String> loadPredicatesSet(File file) throws IOException {
        Set<String> set = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        String[] split;
        while (reader.ready()) {
            line = reader.readLine();
            if (!line.startsWith("#")) {
                split = line.split("\t");
                set.add(split[0]);
            }
        }
        reader.close();
        return set;
    }

    public static double diversityScore(Set<String> userSet, Set<String> itemSet) {
        return diversityScore(userSet, itemSet, 0.5, 0.5);
    }

    public static double diversityScore(Set<String> userSet, Set<String> itemSet, double a, double b) {
        Set<String> itersection = new HashSet<String>(userSet);
        Set<String> union = new HashSet<String>(userSet);
        union.addAll(itemSet);
        itersection.retainAll(itemSet);
        double sim = 0;
        if (union.size() > 0) {
            sim = (double) itersection.size() / (double) union.size();
        }
        Set<String> diff = new HashSet<String>(itemSet);
        diff.removeAll(userSet);
        double nov = 0;
        if (itemSet.size() > 0) {
            nov = (double) diff.size() / (double) itemSet.size();
        }
        return a * sim + b * nov;
    }

    public static double jaccard(Set<String> a, Set<String> b) {
        Set<String> itersection = new HashSet<String>(a);
        Set<String> union = new HashSet<String>(a);
        union.addAll(b);
        itersection.retainAll(b);
        double sim = 0;
        if (union.size() > 0) {
            sim = (double) itersection.size() / (double) union.size();
        }
        return sim;
    }

    public static float getMaxScore(List<ItemScore> mostPopular) {
        float max = 0;
        for (ItemScore item : mostPopular) {
            if (max < item.getScore()) {
                max = item.getScore();
            }
        }
        return max;
    }

    public static float getTotalScore(List<ItemScore> mostPopular) {
        float s = 0;
        for (ItemScore item : mostPopular) {
            s += item.getScore();
        }
        return s;
    }

    public static float getPopularScore(List<ItemScore> mostPopular, String id, float norm) {
        ItemScore find = new ItemScore(id, 0);
        int indexOf = mostPopular.indexOf(find);
        if (indexOf >= 0) {
            return mostPopular.get(indexOf).getScore() / norm;
        } else {
            return 0;
        }

    }


}
