package di.uniba.it.lodrecsys.utils.mapping;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by simo on 16/06/15.
 */
public class StatComplete {
    public static void main(String[] args) throws IOException {
//        stats();
        splittingByUser();
    }

    static class TrainingSet {
        private String userID;
        private String artistID;
        private String listeningCount;

        public TrainingSet(String userID, String artistID, String listeningCount) {
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

        public String getListeningCount() {
            return listeningCount;
        }
    }

    private static void splittingByUser() throws IOException {
        LoadProperties.init("lastfm");
        String dir = LoadProperties.DATASETPATH + "/BKP/user_artists.dat";
        List<String> lines = Files.readAllLines(Paths.get(dir),
                Charset.defaultCharset());
        HashMap<String, HashMap<String, String>> userArtists = new HashMap<>(1893);
        for (String line : lines) {
            String[] tagged = line.split("\t");
            TrainingSet map = new TrainingSet(tagged[0], tagged[1], tagged[2]);
            if (!userArtists.containsKey(map.getUserID())) {
                HashMap<String, String> artistsListened = new HashMap<>();
                artistsListened.put(map.getArtistID(), map.getListeningCount());
                userArtists.put(map.getUserID(), artistsListened);
            }
            else {
                HashMap<String, String> artistsListened = userArtists.get(map.getUserID());
                artistsListened.put(map.getArtistID(), map.getListeningCount());
                userArtists.put(map.getUserID(), artistsListened);
            }
        }

        userArtists.remove("userID");

        int count=0;
        for (String s : userArtists.keySet()) {
            HashMap<String,String> a = userArtists.get(s);
            count += a.size();
            System.out.println(s+" "+a.size());
        }
        System.out.println(count);

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
