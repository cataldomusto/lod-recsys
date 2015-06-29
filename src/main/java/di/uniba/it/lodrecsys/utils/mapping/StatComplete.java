package di.uniba.it.lodrecsys.utils.mapping;

import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

/**
 * Created by simo on 16/06/15.
 */
public class StatComplete {
    public static void main(String[] args) throws IOException {
//        stats();
        propStat();
    }

    static class TrainingSet {
        private String userID;
        private String artistID;
        private int listeningCount;

        public TrainingSet(String userID, String artistID, int listeningCount) {
            this.userID = userID;
            this.artistID = artistID;
            this.listeningCount = listeningCount;
        }

        public String getUserID() {
            return userID;
        }

        public String getArtistID() {
            return artistID;
        }

        public int getListeningCount() {
            return listeningCount;
        }
    }

    private static final double PROP = 0.6;

    private static void splittingByItem() throws IOException {
        LoadProperties.init("lastfm");

        File dirDataset = new File(LoadProperties.MAPPINGPATH);

        List<String> lines = Files.readAllLines(Paths.get(dirDataset + "/datasetFull"),
                Charset.defaultCharset());

        HashMap<String, HashMap<String, Integer>> userArtists = new HashMap<>(1893);
        for (String line : lines) {
            String[] tagged = line.split("\t");
            if (!tagged[0].contains("userID")) {
                TrainingSet map = new TrainingSet(tagged[0], tagged[1], Integer.parseInt(tagged[2]));

                if (!userArtists.containsKey(map.getArtistID())) {
                    HashMap<String, Integer> artistsListened = new HashMap<>();
                    if (map.getListeningCount() == 1) {
                        artistsListened.put("POS", 1);
                        artistsListened.put("NEG", 0);
                    } else {
                        artistsListened.put("POS", 0);
                        artistsListened.put("NEG", 1);
                    }
                    userArtists.put(map.getUserID(), artistsListened);
                } else {

                    HashMap<String, Integer> artistsListened = userArtists.get(map.getArtistID());
                    if (map.getListeningCount() == 1) {
                        int pos = artistsListened.get("POS");
                        pos++;
                        artistsListened.put("POS", pos);
                    } else {
                        int neg = artistsListened.get("NEG");
                        neg++;
                        artistsListened.put("NEG", neg);
                    }

                    userArtists.put(map.getArtistID(), artistsListened);
                }
            }
        }


        for (String artistID : userArtists.keySet()) {
            HashMap<String, Integer> artistListed = userArtists.get(artistID);
//            System.out.println(userID + " " + +artistListed.size() + " SUM " + sum);
            int tot = artistListed.get("POS") + artistListed.get("NEG");

//            System.out.println(artistID + " POS:" + artistListed.get("POS") + " NEG:" + artistListed.get("NEG") + " TOT:" + tot);
            System.out.println(artistListed.get("NEG"));
        }
//        int rap = ((posTOT * 100) / (posTOT + negTOT));
//        System.out.println("POS: " + posTOT + " NEGTOT: " + negTOT + " RAPP:" + rap);
    }

    private static void splittingByUser() throws IOException {
        LoadProperties.init("lastfm");
        File dirDataset = new File(LoadProperties.MAPPINGPATH);

        if (new File(dirDataset + "/datasetFull").exists()) {
            new File(dirDataset + "/datasetFull").delete();
        }
        PrintWriter writerDatasetFull = new PrintWriter(dirDataset + "/datasetFull", "UTF-8");

        if (new File(dirDataset + "/trainingset").exists()) {
            new File(dirDataset + "/trainingset").delete();
        }
        PrintWriter writerTraining = new PrintWriter(dirDataset + "/trainingset", "UTF-8");

        if (new File(dirDataset + "/testset").exists()) {
            new File(dirDataset + "/testset").delete();
        }
        PrintWriter writerTest = new PrintWriter(dirDataset + "/testset", "UTF-8");

        String dir = LoadProperties.DATASETPATH + "/BKP/user_artists.dat";
        List<String> lines = Files.readAllLines(Paths.get(dir),
                Charset.defaultCharset());
        HashMap<String, HashMap<String, Integer>> userArtists = new HashMap<>(1893);
        for (String line : lines) {
            String[] tagged = line.split("\t");
            if (!tagged[0].contains("userID")) {
                TrainingSet map = new TrainingSet(tagged[0], tagged[1], Integer.parseInt(tagged[2]));
                if (!userArtists.containsKey(map.getUserID())) {
                    HashMap<String, Integer> artistsListened = new HashMap<>();
                    artistsListened.put(map.getArtistID(), map.getListeningCount());
                    userArtists.put(map.getUserID(), artistsListened);
                } else {
                    HashMap<String, Integer> artistsListened = userArtists.get(map.getUserID());
                    artistsListened.put(map.getArtistID(), map.getListeningCount());
                    userArtists.put(map.getUserID(), artistsListened);
                }
            }
        }

        int posTOT = 0, negTOT = 0;
        for (String userID : userArtists.keySet()) {
            int sum = 0;
            HashMap<String, Integer> artistListed = userArtists.get(userID);
            for (String artistID : artistListed.keySet()) {
                sum += artistListed.get(artistID);
            }
//            System.out.println(userID + " " + +artistListed.size() + " SUM " + sum);

            float avg = sum / artistListed.size();
            float pos = 0, neg = 0;
            for (String artistID : artistListed.keySet()) {
                if (artistListed.get(artistID) >= (avg * PROP)) {
                    writerDatasetFull.write(userID + "\t" + artistID + "\t" + "1\n");
                    pos++;
                    posTOT++;
                } else {
                    writerDatasetFull.write(userID + "\t" + artistID + "\t" + "0\n");
                    neg++;
                    negTOT++;
                }
            }

            int maxCountPOS = (int) Math.round(pos * 0.7);
            int maxCountNEG = (int) Math.round(neg * 0.7);

            int posCount = 0, negCount = 0;
            for (String artistID : artistListed.keySet()) {
                if (artistListed.get(artistID) >= (avg * PROP)) {
                    if (posCount < maxCountPOS) {
                        posCount++;
                        writerTraining.write(userID + "\t" + artistID + "\t" + "1\n");
                    } else
                        writerTest.write(userID + "\t" + artistID + "\t" + "1\n");
                } else {
                    if (negCount < maxCountNEG) {
                        negCount++;
                        writerTraining.write(userID + "\t" + artistID + "\t" + "0\n");
                    } else
                        writerTest.write(userID + "\t" + artistID + "\t" + "0\n");
                }
            }
            System.out.println(userID + " POS:" + pos + " NEG:" + neg + " AVG:" + avg + " TOT:" + artistListed.size() + " POSTRAIN: " + posCount);
        }
//        int rap = ((posTOT * 100) / (posTOT + negTOT));
//        System.out.println("POS: " + posTOT + " NEGTOT: " + negTOT + " RAPP:" + rap);
        writerDatasetFull.close();
        writerTest.close();
        writerTraining.close();
    }

    private static void propStat() throws IOException {
        LoadProperties.init("lastfm");

        File dirDataset = new File(LoadProperties.MAPPINGPATH);

        List<String> linesAll = Files.readAllLines(Paths.get(dirDataset + "/all_prop"),
                Charset.defaultCharset());

        HashMap<String, Integer> propCount = new HashMap<>(1893);
        for (String line : linesAll)
            propCount.put(line, 0);

        List<String> lines50 = Files.readAllLines(Paths.get(dirDataset + "/prop10"),
                Charset.defaultCharset());

        for (String s : lines50) {
            int count = propCount.get(s);
            count++;
            propCount.put(s, count);
        }

        for (String s : propCount.keySet()) {
//            System.out.println(s);
            System.out.println(propCount.get(s));
        }
    }

    private static void stats() throws FileNotFoundException, UnsupportedEncodingException {
        LoadProperties.init("lastfm");

        File dir = new File(LoadProperties.MAPPINGPATH);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.contains("stat");
            }
        });

        HashMap<String, Integer> props = new HashMap<>(70);

        for (File file : files) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()),
                        Charset.defaultCharset());
                for (String line : lines) {
                    String[] strings = line.split(" ");
                    if (props.containsKey(strings[1])) {
                        props.put(strings[1], props.get(strings[1]) + Integer.valueOf(strings[0]));
                    } else
                        props.put(strings[1], Integer.valueOf(strings[0]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (new File(dir + "/statCOMPLETE").exists()) {
            new File(dir + "/statCOMPLETE").delete();
        }

        PrintWriter writer = new PrintWriter(dir + "/statCOMPLETE", "UTF-8");
        for (String s : props.keySet()) {
            writer.write(props.get(s) + " " + s + "\n");
        }
        writer.close();
    }
}
