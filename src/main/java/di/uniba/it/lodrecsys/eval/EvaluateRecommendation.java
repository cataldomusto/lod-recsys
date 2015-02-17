package di.uniba.it.lodrecsys.eval;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.graph.Edge;
import di.uniba.it.lodrecsys.utils.CmdExecutor;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import di.uniba.it.lodrecsys.utils.Utils;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import static di.uniba.it.lodrecsys.graph.GraphRecRun.savefileLog;

/**
 * Created by asuglia on 4/4/14.
 * Modded by Simone Rutigliano
 */
public class EvaluateRecommendation {

    private static final String PATHTREC = "./datasets/";

    private static Logger logger = Logger.getLogger(EvaluateRecommendation.class.getName());

    private static HashMap<String, HashMap<String, Integer>> loadPropFilm(int numRec) {
        String dir;
        switch (LoadProperties.FILTERTYPE) {
            case "RankerWeka":
                dir = "./mapping/choosen_prop/choosen_prop" + LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + LoadProperties.EVALWEKA;
                break;
            case "CFSubsetEval":
                dir = "./mapping/choosen_prop/choosen_prop" + LoadProperties.FILTERTYPE;
                break;
            default:
                dir = "./mapping/choosen_prop/choosen_prop" + LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER;
                break;
        }
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(dir),
                    Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, HashMap<String, Integer>> mappingFilmPropCount = new HashMap<>(numRec);

        FileInputStream fis = null;
        try {
            fis = new FileInputStream("./serialized/graphComplete.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        UndirectedSparseMultigraph<String, Edge> recGraph = null;
        try {
            recGraph = (UndirectedSparseMultigraph<String, Edge>) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert recGraph != null;
        Collection<Edge> recGraphEdges = recGraph.getEdges();

        for (Edge s : recGraphEdges) {
            mappingFilmPropCount.put(s.getSubject(), new HashMap<String, Integer>());
        }

//        String film = "http://dbpedia.org/resource/Shall_We_Dance%3F_(1996_film)";
        for (String film : mappingFilmPropCount.keySet()) {
            HashMap<String, Integer> mappingPropCount = new HashMap<>(lines.size());

            for (String line : lines) {
                mappingPropCount.put(line, 0);
            }

            Collection<Edge> propsFilm = recGraph.getIncidentEdges(film);
            for (Edge edge : propsFilm) {
                if (edge.getSubject().equals(film)) {
                    if (lines.contains(edge.getProperty())) {
                        int v = mappingPropCount.get(edge.getProperty());
                        v++;
                        mappingPropCount.put(edge.getProperty(), v);
                    }
                }
            }
            mappingFilmPropCount.put(film, mappingPropCount);
        }
        return mappingFilmPropCount;
    }

    private static String uriByID(String id, HashMap<String, HashMap<String, Integer>> mapFilmCountProp) {
        try {
            MovieMapping movieMapping = Utils.findMovieMappingbyId(id);
            if (movieMapping != null && mapFilmCountProp.containsKey(movieMapping.getDbpediaURI()))
                return movieMapping.getDbpediaURI();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String uriByID(String id) {
        try {
            MovieMapping movieMapping = Utils.findMovieMappingbyId(id);
            if (movieMapping != null)
                return movieMapping.getDbpediaURI();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HashMap<String, Double> ildmetric(Map<String, Set<Rating>> recommendationList, int numRec, HashMap<String, HashMap<String, Integer>> mapFilmCountProp) throws IOException {

//        String userID = "184";
        HashMap<String, Double> ildAllUser = new HashMap<>(900);
        for (String userID : recommendationList.keySet()) {
            ArrayList<String> itemRec = new ArrayList<>(numRec);
            Set<Rating> recommendationListForUser = recommendationList.get(userID);
            int i = 0;
            for (Rating rate : recommendationListForUser) {
                String uri = uriByID(rate.getItemID(), mapFilmCountProp);
                if (uri != null) {
//                    System.out.println("userID " + userID + " : Film " + uri);
                    itemRec.add(uri);
                    i++;
                }
                // prints only numRec recommendation on file
                if (numRec != -1 && i >= numRec)
                    break;
            }

//            System.out.println("-----------------");
            double similarityTot = 0f;
            int nRec = 0;
            for (int i1 = 0; i1 < itemRec.size() - 1; i1++) {
                for (int j = i1 + 1; j < itemRec.size(); j++) {
//                    System.out.println(itemRec.get(i1) + " " + itemRec.get(j));
                    double val = cosSimMetric(mapFilmCountProp.get(itemRec.get(i1)), mapFilmCountProp.get(itemRec.get(j)));
                    if (val != 0.0) {
                        nRec++;
//                        System.out.println("cosSim(" + itemRec.get(i1) + " " + itemRec.get(j) + ") = " + val);
                        similarityTot = similarityTot + val;
                    }
                }
            }
//            System.out.println("-----------------");
//            System.out.println("SimTot = " + similarityTot);
//            System.out.println("nRec = " + nRec);
            double simAVG = 0;
            if (nRec != 0)
                simAVG = similarityTot / nRec;
//            System.out.println("SimAVG = " + simAVG);
            double ildUser = 1 - simAVG;
//            System.out.println("Diversity = 1 - " + simAVG + " = " + ildUser);
            ildAllUser.put(userID, ildUser);
        }
        return ildAllUser;
    }

    private static double avgILD(HashMap<String, Double> ildAllUser) {
        double avg = 0;
        for (Double aDouble : ildAllUser.values())
            avg += aDouble;
        avg = avg / ildAllUser.size();
        return avg;
    }

    private static double cosSimMetric(HashMap<String, Integer> film1, HashMap<String, Integer> film2) {

        ArrayList<Integer> valuesFilm1 = new ArrayList<>();
        for (String s : film1.keySet()) {
            valuesFilm1.add(film1.get(s));
//            System.out.println("Prop: "+ s + " Val: "+film1.get(s));
        }

//        System.out.println("----------------");

        ArrayList<Integer> valuesFilm2 = new ArrayList<>();
        for (String s : film2.keySet()) {
            valuesFilm2.add(film2.get(s));
//            System.out.println("Prop: "+ s + " Val: "+film2.get(s));
        }

//        System.out.println("----------------");

        double num = 0;
        for (int i = 0; i < valuesFilm1.size(); i++) {
            num += valuesFilm1.get(i) * valuesFilm2.get(i);
        }

//        System.out.println("Numeratore : " + num);

        int sumA = 0;
        ArrayList<Integer> valuesFilm1quad = new ArrayList<>();
        for (Integer value : valuesFilm1) {
            valuesFilm1quad.add((int) Math.pow(value, 2.0));
            sumA += (int) Math.pow(value, 2.0);
        }
        int sumB = 0;
        ArrayList<Integer> valuesFilm2quad = new ArrayList<>();
        for (Integer value : valuesFilm2) {
            valuesFilm2quad.add((int) Math.pow(value, 2.0));
            sumB += (int) Math.pow(value, 2.0);
        }

        double denA = Math.sqrt(sumA);
        double denB = Math.sqrt(sumB);
        double den = denA * denB;
//        System.out.println("Denominatore : " + denA +" * " + denB +" = "+den);
        if (den != 0) {
//            System.out.println("CosSim : " + num + " / " + den + " = " + num / den);
            return num / den;
        } else
            return 0.0;
    }

    public static ArrayList<HashMap<String, HashMap<String, Integer>>> mapFilmCount() {
        int[] cutoffLevels = new int[]{5, 10, 15, 20, 30, 50};
        ArrayList<HashMap<String, HashMap<String, Integer>>> arrayList = new ArrayList<>(cutoffLevels.length);
        for (int cutoffLevel : cutoffLevels) {
            HashMap<String, HashMap<String, Integer>> mapFilmCountProp = loadPropFilm(cutoffLevel);
            arrayList.add(mapFilmCountProp);
        }
        return arrayList;
    }

    public static HashMap<String, String> evalMSIMeasure(Map<String, Set<Rating>> recommendationList) {
        String measuresAll = "", measuresAVG = "";
        int[] cutoffLevels = new int[]{5, 10, 15, 20, 30, 50};
        for (int i = 0; i < cutoffLevels.length; i++) {
            int cutoffLevel = cutoffLevels[i];
            HashMap<String, Double> usersMSI = null;
            double avgMeasure = 0.0;
            try {
                usersMSI = msimetric(recommendationList, cutoffLevel);
                avgMeasure = avgMSI(usersMSI);

            } catch (IOException e) {
                e.printStackTrace();
            }
            measuresAVG += "Novelty_" + cutoffLevel + "," + avgMeasure + "\n";
            assert usersMSI != null;
            for (String s : usersMSI.keySet())
                measuresAll += s + ",Novelty_" + cutoffLevel + "," + usersMSI.get(s) + "\n";
//            System.out.println("Novelty_" + cutoffLevel + "," + avgMeasure);
        }

        HashMap<String, String> measures = new HashMap<>(2);
        measures.put("all", measuresAll.substring(0, measuresAll.length() - 1));
        measures.put("avg", measuresAVG.substring(0, measuresAVG.length() - 1));
        return measures;
    }

//    public static String evalMSIMeasureAll(Map<String, Set<Rating>> recommendationList) {
//        String measures = "";
//        int[] cutoffLevels = new int[]{5, 10, 15, 20, 30, 50};
//        for (int i = 0; i < cutoffLevels.length; i++) {
//            int cutoffLevel = cutoffLevels[i];
//            HashMap<String, Double> usersMSI = null;
//            try {
//                usersMSI = msimetric(recommendationList, cutoffLevel);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            for (String s : usersMSI.keySet())
//                measures += s + ",Novelty_" + cutoffLevel + "," + usersMSI.get(s) + "\n";
////            System.out.println("Novelty_" + cutoffLevel + "," + avgMeasure);
//        }
//        return measures.substring(0, measures.length() - 1);
//    }

    private static HashMap<String, Double> msimetric(Map<String, Set<Rating>> recommendationList, int numRec) throws IOException {

        HashMap<String, Double> allRecID = new HashMap<>(1400);
        for (String userID : recommendationList.keySet()) {
            Set<Rating> recommendationListForUser = recommendationList.get(userID);
            int i = 0;
            for (Rating rate : recommendationListForUser) {
                if (!allRecID.containsKey(rate.getItemID()))
                    allRecID.put(rate.getItemID(), 1.0);
                else {
                    double value = allRecID.get(rate.getItemID());
                    value++;
                    allRecID.put(rate.getItemID(), value);
                }
                i++;
                // prints only numRec recommendation on file
                if (numRec != -1 && i >= numRec)
                    break;
            }
        }

        int totUser = recommendationList.size();
        for (String s : allRecID.keySet()) {
            double prop = allRecID.get(s) / totUser;
//            System.out.println("Film "+s + " NItems "+allRecID.get(s) + " / "+totUser + " = "+prop);
            allRecID.put(s, prop);
        }

//        String userID = "446";    USER da 1 rec
//        String userID = "185";    //USER da n rec
        HashMap<String, Double> msiUsers = new HashMap<>(900);
        for (String userID : recommendationList.keySet()) {
            double noveltyM = 0.0;
            Set<Rating> recommendationListForUser = recommendationList.get(userID);
            int i = 0;
            for (Rating rate : recommendationListForUser) {
//            System.out.println("FilmID: " + rate.getItemID() + " log(" + allRecID.get(rate.getItemID()) + ") : " + Math.log(allRecID.get(rate.getItemID())));
                noveltyM += (Math.log(allRecID.get(rate.getItemID())));
                i++;

                // prints only numRec recommendation on file
                if (numRec != -1 && i >= numRec) {
                    break;
                }
            }
            double noveltyAVG = -(noveltyM) / ((double) i);
            msiUsers.put(userID, noveltyAVG);
//        System.out.println("sum novelty: " + noveltyM);
//        System.out.println("divisore: " + i);
//        System.out.println("avg novelty: " + noveltyAVG);
        }
        return msiUsers;
    }


    private static double avgMSI(HashMap<String, Double> msiUsers) {
        double avg = 0;
        for (Double aDouble : msiUsers.values())
            avg += aDouble;
        avg = avg / msiUsers.size();
        return avg;
    }


    public static HashMap<String, String> evalILDMeasure(Map<String, Set<Rating>> recommendationList, ArrayList<HashMap<String, HashMap<String, Integer>>> mapFilmCount) {
        String measuresAll = "", measuresAVG = "";
        int[] cutoffLevels = new int[]{5, 10, 15, 20, 30, 50};
        for (int i = 0; i < cutoffLevels.length; i++) {
            int cutoffLevel = cutoffLevels[i];
            HashMap<String, HashMap<String, Integer>> mapFilmCountProp = mapFilmCount.get(i);
            double avgMeasure = 0;
            HashMap<String, Double> usersILD = null;
            try {
                usersILD = ildmetric(recommendationList, cutoffLevel, mapFilmCountProp);
                avgMeasure = avgILD(usersILD);

            } catch (IOException e) {
                e.printStackTrace();
            }
            measuresAVG += "Diversity_" + cutoffLevel + "," + avgMeasure + "\n";
            assert usersILD != null;
            for (String s : usersILD.keySet())
                measuresAll += s + ",Diversity_" + cutoffLevel + "," + usersILD.get(s) + "\n";
//            System.out.println("Diversity_" + cutoffLevel + "," + avgMeasure);
        }
        HashMap<String, String> measures = new HashMap<>(2);
        measures.put("all", measuresAll.substring(0, measuresAll.length() - 1));
        measures.put("avg", measuresAVG.substring(0, measuresAVG.length() - 1));
        return measures;
    }

//    public static String evalILDMeasureAll(Map<String, Set<Rating>> recommendationList, ArrayList<HashMap<String, HashMap<String, Integer>>> mapFilmCount) {
//        String measures = "";
//        int[] cutoffLevels = new int[]{5, 10, 15, 20, 30, 50};
//        for (int i = 0; i < cutoffLevels.length; i++) {
//            int cutoffLevel = cutoffLevels[i];
//            HashMap<String, HashMap<String, Integer>> mapFilmCountProp = mapFilmCount.get(i);
//            HashMap<String, Double> usersILD = null;
//            try {
//                usersILD = ildmetric(recommendationList, cutoffLevel, mapFilmCountProp);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            for (String s : usersILD.keySet())
//                measures += s + ",Diversity_" + cutoffLevel + "," + usersILD.get(s) + "\n";
////            System.out.println("Diversity_" + cutoffLevel + "," + avgMeasure);
//        }
//        return measures.substring(0, measures.length() - 1);
//    }

    public static void saveEvalILDMeasure(String ild, String resFile) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(resFile + "3", true)))) {
            out.println(ild);
        } catch (IOException e) {
        }
    }

    public static void saveEvalMSIMeasure(String msi, String resFile) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(resFile + "4", true)))) {
            out.println(msi);
        } catch (IOException e) {
        }
    }

    public static void saveEvalSerendipityMeasure(String serendipity, String resFile) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(resFile + "5", true)))) {
            out.println(serendipity);
        } catch (IOException e) {
        }
    }

    /**
     * Serializes a specific number of recommendation for each user according to
     * the TREC evaluation file format
     *
     * @param recommendationList all the recommendation for each user
     * @param resFile            the result's filename
     * @param numRec             number of recommendation that will be saved (-1 if all of them needed)
     * @throws IOException unable to write the result file
     */
    public static void serializeRatings(Map<String, Set<Rating>> recommendationList, String resFile, int numRec) throws IOException {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(resFile));


            for (String userID : recommendationList.keySet()) {
                Set<Rating> recommendationListForUser = recommendationList.get(userID);
                int i = 0;
                for (Rating rate : recommendationListForUser) {
                    String trecLine = userID + " Q0 " + rate.getItemID() + " " + i++ + " " + rate.getRating() + " EXP";
                    writer.write(trecLine);
                    writer.newLine();

                    // prints only numRec recommendation on file
                    if (numRec != -1 && i == numRec)
                        break;
                }
            }
        } catch (IOException e) {
            logger.severe(e.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }


    }


    /**
     * Transforms the MyMediaLite prediction's file into
     * a TREC eval results file format.
     * <p/>
     * Trec eval results format
     * <id_user> Q0 <id_item> <posizione nel rank> <score> <nome esperimento>
     */
    public static void generateTrecEvalFile(String resultFile, String outTrecFile) throws IOException {
        PrintWriter writer = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(resultFile));
            writer = new PrintWriter(new FileWriter(outTrecFile));

            String currUser = "";
            Set<Rating> currUserRatings = new TreeSet<>();

            while (reader.ready()) {
                String line = reader.readLine();
                String[] lineSplitted = line.split("\t");
                String userID = lineSplitted[0];

                String ratingString = lineSplitted[1].substring(lineSplitted[1].indexOf("[") + 1, lineSplitted[1].indexOf("]"));

                Set<Rating> ratings = getRatingsSet(ratingString.split(","));
                int i = 0;

                for (Rating rate : ratings) {

                    String trecLine = userID + " Q0 " + rate.getItemID() + " " + i++ + " " + rate.getRating() + " EXP";

                    writer.println(trecLine);
                }
            }
        } catch (IOException ex) {
            throw new IOException(ex);
        } finally {
            assert reader != null;
            reader.close();
            assert writer != null;
            writer.close();
        }


    }

    /**
     * Transforms an array of strings which contains prediction in the format
     * item_id:rating, into a set of Rating
     *
     * @param ratings array of rating in string form
     * @return an ordered list of ratings
     */
    private static Set<Rating> getRatingsSet(String[] ratings) {
        Set<Rating> ratingSet = new TreeSet<>();

        for (String rating : ratings) {
            String splitted[] = rating.split(":");
            ratingSet.add(new Rating(splitted[0], splitted[1]));
        }

        return ratingSet;

    }

    /**
     * Executes the trec_eval tool to evaluate the produced results
     * computing per user metrics and saves all in a file
     *
     * @param goldStandardFile filename of the test file in trec_eval format
     * @param resultFile       filename of the results file in trec_eval format
     * @param trecResultFile   filename of the results produced by trec_eval
     */
    public static void savePerUserTrec(String goldStandardFile, String resultFile, String trecResultFile) {
        savefileLog(goldStandardFile);
        String trecEvalCommand = PATHTREC + "trec_eval -q -m all_trec " + goldStandardFile + " " + resultFile;
        CmdExecutor.executeCommandAndPrintLinux(trecEvalCommand, trecResultFile);
//        logger.info(trecEvalCommand);
    }

    /**
     * Executes the ndeval tool to evaluate the produced results
     * and saves them in a file
     *
     * @param goldStandardFile filename of the test file in trec_eval format
     * @param resultFile       filename of the results file in trec_eval format
     * @param trecResultFile   filename of the results produced by trec_eval
     */
    public static void saveTrecNdevalResult(String goldStandardFile, String resultFile, String trecResultFile) {
        String resTemp = trecResultFile + "2";
        String trecEvalCommand = PATHTREC + "ndeval " + goldStandardFile + " " + resultFile + " >> " + trecResultFile + "Temp1";
        CmdExecutor.executeCommand(trecEvalCommand, false);

        String cmdMod = "head -1 " + trecResultFile + "Temp1 > " + resTemp;
        CmdExecutor.executeCommand(cmdMod, false);

        cmdMod = "tail -1 " + trecResultFile + "Temp1 >> " + resTemp;
        CmdExecutor.executeCommand(cmdMod, false);

        new File(trecResultFile + "Temp1").delete();

//        logger.info(trecEvalCommand);
    }

    /**
     * Executes the trec_eval tool to evaluate the produced results
     * and saves them in a file
     *
     * @param goldStandardFile filename of the test file in trec_eval format
     * @param resultFile       filename of the results file in trec_eval format
     * @param trecResultFile   filename of the results produced by trec_eval
     */
    public static void saveTrecEvalResult(String goldStandardFile, String resultFile, String trecResultFile) {
        String resTemp = trecResultFile + "Temp";
        String trecEvalCommand = PATHTREC + "trec_eval -m all_trec " + goldStandardFile + " " + resultFile;
        CmdExecutor.executeCommandAndPrint(trecEvalCommand, trecResultFile);

        trecEvalCommand = PATHTREC + "trec_eval -m P.50 " + goldStandardFile + " " + resultFile;
        CmdExecutor.executeCommandAndPrint(trecEvalCommand, resTemp);

        CmdExecutor.executeCommand("cat " + resTemp + " >> " + trecResultFile, false);
        new File(resTemp).delete();

        trecEvalCommand = PATHTREC + "trec_eval -m recall.50 " + goldStandardFile + " " + resultFile;
        CmdExecutor.executeCommandAndPrint(trecEvalCommand, resTemp);

        CmdExecutor.executeCommand("cat " + resTemp + " >> " + trecResultFile, false);
        new File(resTemp).delete();

        saveTrecNdevalResult(goldStandardFile, resultFile, trecResultFile);

//        logger.info(trecEvalCommand);
    }

    /**
     * Parses the specified trec eval results file and retrives all the produced
     * metrics
     *
     * @param trecEvalFile filename of the trec_eval results file
     * @return a dictionary whose keys are metrics' name and whose values are metrics' values
     * @throws IOException
     */
    public static Map<String, String> getTrecEvalResults(String trecEvalFile) throws IOException {
        CSVParser parser = null;
        Map<String, String> trecMetrics = new HashMap<>();

        try {
            // trec_eval loading
            if (new File(trecEvalFile).exists()) {
                parser = new CSVParser(new FileReader(trecEvalFile), CSVFormat.TDF);
//            logger.info("Loading trec eval metrics from: " + trecEvalFile);
                for (CSVRecord rec : parser.getRecords()) {
                    trecMetrics.put(rec.get(0), rec.get(2));
                }
            }

            // ndeval loading
            if (new File(trecEvalFile + "2").exists()) {
                parser = new CSVParser(new FileReader(trecEvalFile + "2"), CSVFormat.newFormat(','));
//            logger.info("Loading trec eval metrics from: " + trecEvalFile);
                List<CSVRecord> records = parser.getRecords();
                for (int i = 0; i < records.get(0).size(); i++) {
                    trecMetrics.put(records.get(0).get(i), records.get(1).get(i));
                }
            }

            // Diversity loading
            if (new File(trecEvalFile + "3").exists()) {
                parser = new CSVParser(new FileReader(trecEvalFile + "3"), CSVFormat.newFormat(','));
//            logger.info("Loading trec eval metrics from: " + trecEvalFile);
                for (CSVRecord rec : parser.getRecords()) {
                    trecMetrics.put(rec.get(0), rec.get(1));
                }
            }

            // Novelty loading
            if (new File(trecEvalFile + "4").exists()) {
                parser = new CSVParser(new FileReader(trecEvalFile + "4"), CSVFormat.newFormat(','));
//            logger.info("Loading trec eval metrics from: " + trecEvalFile);
                for (CSVRecord rec : parser.getRecords()) {
                    trecMetrics.put(rec.get(0), rec.get(1));
                }
            }

            // Serendipity loading
            if (new File(trecEvalFile + "5").exists()) {
                parser = new CSVParser(new FileReader(trecEvalFile + "5"), CSVFormat.newFormat(','));
//            logger.info("Loading trec eval metrics from: " + trecEvalFile);
                for (CSVRecord rec : parser.getRecords()) {
                    trecMetrics.put(rec.get(0), rec.get(1));
                }
            }

            return trecMetrics;
        } catch (IOException e) {
            throw e;
        } finally {
            assert parser != null;
            parser.close();
        }

    }

    /**
     * Computes F1-measure from the precision and recall specified
     * values
     *
     * @param precision current precision
     * @param recall    current recall
     * @return f1-measure
     */
    public static float getF1(float precision, float recall) {
        return (precision == 0 && precision == recall) ? 0 : (2 * precision * recall) / (precision + recall);

    }

    /**
     * Computes F1-measure for all the cut-off levels defined
     * which are: 5, 10, 15, 20, 30, 50
     *
     * @param measures the metrics' map that will be updated with f1-measures
     */
    private static void evalF1Measure(Map<String, Float> measures) {
        int[] cutoffLevels = new int[]{5, 10, 15, 20, 30, 50};
        String precisionString = "P", recallString = "recall", fMeasureString = "F1";

        for (int cutoff : cutoffLevels) {
            String currPrecision = precisionString + "_" + cutoff,
                    currRecall = recallString + "_" + cutoff;
            if (measures.containsKey(currPrecision) && measures.containsKey(currRecall))
                measures.put(fMeasureString + "_" + cutoff, getF1(measures.get(currPrecision), measures.get(currRecall)));
        }
    }

    /**
     * Averages the metrics results for each split
     *
     * @param metricsValuesForSplit list of metrics computed for each split
     * @param numberOfSplit         number of split
     * @return string representation of the results
     */
    public static String averageMetricsResult(List<Map<String, String>> metricsValuesForSplit, int numberOfSplit) {
        StringBuilder results = new StringBuilder("");
        String[] usefulMetrics = {"P_5", "P_10", "P_15", "P_20", "P_30", "P_50", "recall_5", "recall_10",
                "recall_15", "recall_20", "recall_30", "recall_50", "Diversity_5", "Diversity_10", "Diversity_15", "Diversity_20", "Novelty_5", "Novelty_10", "Novelty_15", "Novelty_20", "Serendipity_5", "Serendipity_10", "Serendipity_15", "Serendipity_20"},
                completeMetrics = {"P_5", "P_10", "P_15", "P_20", "P_30", "P_50", "recall_5", "recall_10",
                        "recall_15", "recall_20", "recall_30", "recall_50", "F1_5", "F1_10", "F1_15", "F1_20", "F1_30", "F1_50", "Diversity_5", "Diversity_10", "Diversity_15", "Diversity_20", "Novelty_5", "Novelty_10", "Novelty_15", "Novelty_20", "Serendipity_5", "Serendipity_10", "Serendipity_15", "Serendipity_20"};

        Map<String, Float> averageRes = new HashMap<>();
        for (String measure : usefulMetrics) {
            float currMetricsTot = 0f;
            for (Map<String, String> map : metricsValuesForSplit) {
                if (map.containsKey(measure))
                    currMetricsTot += Float.parseFloat(map.get(measure));
            }
            if (currMetricsTot != 0f)
                averageRes.put(measure, currMetricsTot / numberOfSplit);
        }

        evalF1Measure(averageRes);

        for (String measure : completeMetrics) {
            results.append(measure.replace("@", "_")).append("=").append(averageRes.get(measure)).append("\n");
        }

        return results.toString();

    }

    /**
     * Averages the metrics results for each split
     *
     * @param metricsValuesForSplit list of metrics computed for each split
     * @return string representation of the results
     */
    public static String averageMetricsResultALL(List<Map<String, HashMap<String, Float>>> metricsValuesForSplit) {
        StringBuilder results = new StringBuilder("");
//        String[] usefulMetrics = {"F1_5", "F1_10", "F1_15", "F1_20", "F1_30", "F1_50", "Diversity_5", "Diversity_10", "Diversity_15", "Diversity_20", "Novelty_5", "Novelty_10", "Novelty_15", "Novelty_20", "Serendipity_5", "Serendipity_10", "Serendipity_15", "Serendipity_20"};
        String[] usefulMetrics = {"P_5", "P_10", "P_15", "P_20", "P_30", "P_50", "recall_5", "recall_10",
                "recall_15", "recall_20", "recall_30", "recall_50", "F1_5", "F1_10", "F1_15", "F1_20", "F1_30", "F1_50", "Diversity_5", "Diversity_10", "Diversity_15", "Diversity_20", "Novelty_5", "Novelty_10", "Novelty_15", "Novelty_20"},
                completeMetrics = {"F1_5", "F1_10", "F1_15", "F1_20", "F1_30", "F1_50", "Diversity_5", "Diversity_10", "Diversity_15", "Diversity_20", "Novelty_5", "Novelty_10", "Novelty_15", "Novelty_20"};

        ArrayList<String> users = new ArrayList<>();
        for (Map<String, HashMap<String, Float>> map : metricsValuesForSplit) {
            for (String s : map.keySet()) {
                if (!users.contains(s))
                    users.add(s);
            }
        }
        Map<String, HashMap<String, Float>> averageRes = new HashMap<>();
        for (String user : users) {
            HashMap<String, Float> measures = new HashMap<>(usefulMetrics.length);
            for (String measure : usefulMetrics) {
                float currMetricsTot = 0f;
                int i = 0;
                for (Map<String, HashMap<String, Float>> map : metricsValuesForSplit) {
                    if (map.containsKey(user)) {
                        HashMap<String, Float> userF = map.get(user);
                        if (userF.containsKey(measure)) {
                            currMetricsTot += userF.get(measure);
                            i++;
                        }
                    }
                }

                if (currMetricsTot != 0f) {
                    measures.put(measure, currMetricsTot / i);
                }
            }
            averageRes.put(user, measures);

            evalF1Measure(averageRes.get(user));

            for (String finalMeasure : completeMetrics) {
                results.append(user).append(finalMeasure).append("=").append(averageRes.get(user).get(finalMeasure)).append("\n");
            }
        }

//        for (String s : trecMetrics.keySet()) {
//            HashMap<String, String> metrics = trecMetrics.get(s);
//            HashMap<String, Float> fmetricsval = new HashMap<>(6);
//            float fUID = getF1(Float.parseFloat(metrics.get("P_5")), Float.parseFloat(metrics.get("recall_5")));
//            fmetricsval.put("F1_5", fUID);
//            fUID = getF1(Float.parseFloat(metrics.get("P_10")), Float.parseFloat(metrics.get("recall_10")));
//            fmetricsval.put("F1_10", fUID);
//            fUID = getF1(Float.parseFloat(metrics.get("P_15")), Float.parseFloat(metrics.get("recall_15")));
//            fmetricsval.put("F1_15", fUID);
//            fUID = getF1(Float.parseFloat(metrics.get("P_20")), Float.parseFloat(metrics.get("recall_20")));
//            fmetricsval.put("F1_20", fUID);
//            fUID = getF1(Float.parseFloat(metrics.get("P_30")), Float.parseFloat(metrics.get("recall_30")));
//            fmetricsval.put("F1_30", fUID);
//            fUID = getF1(Float.parseFloat(metrics.get("P_50")), Float.parseFloat(metrics.get("recall_50")));
//            fmetricsval.put("F1_50", fUID);
//            fmetrics.put(s, fmetricsval);
//        }


        return results.toString();
    }


    /**
     * Serializes the metrics results coming from the evaluation process in a file
     *
     * @param metricsResult      string representation of the results
     * @param completeReportFile filename of the metrics results
     * @throws IOException if unable to write the file
     */
    public static void generateMetricsFile(String metricsResult, String completeReportFile) throws IOException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(completeReportFile));
            writer.write(metricsResult);

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            assert writer != null;
            writer.close();

        }
    }


    public static String evalSerMeasure(Map<String, Set<Rating>> recommendationList) {
        UndirectedSparseMultigraph<String, Edge> recGraph = graphFiltered();
        String measures = "";
//        int[] cutoffLevels = new int[]{5, 10, 15, 20, 30, 50};
        int[] cutoffLevels = new int[]{10};
        for (int i = 0; i < cutoffLevels.length; i++) {
            int cutoffLevel = cutoffLevels[i];
            double avgMeasure = 0;
            try {
                avgMeasure = serendipityMetric(recommendationList, cutoffLevel, recGraph);
            } catch (IOException e) {
                e.printStackTrace();
            }
            measures += "Serendipity_" + cutoffLevel + "," + avgMeasure + "\n";
//            System.out.println("Serendipity_" + cutoffLevel + "," + avgMeasure);
        }
        return measures.substring(0, measures.length() - 1);
    }

    private static double serendipityMetric(Map<String, Set<Rating>> recommendationList, int numRec, UndirectedSparseMultigraph<String, Edge> recGraph) throws IOException {

//        String userID = "453";
        ArrayList<Double> serendipityAllUser = new ArrayList<>();
        for (String userID : recommendationList.keySet()) {
            ArrayList<String> itemRec = new ArrayList<>(numRec);
            Set<Rating> recommendationListForUser = recommendationList.get(userID);
            int i = 0;
            for (Rating rate : recommendationListForUser) {
                String uri = uriByID(rate.getItemID());
                if (uri != null) {
//                    System.out.println("userID " + userID + " : Film " + uri);
                    itemRec.add(uri);
                    i++;
                }
                // prints only numRec recommendation on file
                if (numRec != -1 && i >= numRec)
                    break;
            }

//            System.out.println("-----------------");
            double serendipityTot = 0f;
            int nRec = 0;
            for (int i1 = 0; i1 < itemRec.size() - 1; i1++) {
                for (int j = i1 + 1; j < itemRec.size(); j++) {
                    double val = shortestPathMetric(itemRec.get(i1), itemRec.get(j), recGraph);
//                System.out.println("Shortest(" + itemRec.get(i1) + " " + itemRec.get(j) + ") = "+val);
                    if (val != 0.0) {
                        nRec++;
                        serendipityTot = serendipityTot + val;
                    }
                }
            }
//        System.out.println("-----------------");
//        System.out.println("SerTot = " + serendipityTot);
//        System.out.println("nRec = " + nRec);
            double serAVG = 0;
            if (nRec != 0)
                serAVG = serendipityTot / nRec;
//        System.out.println(userID + " SerAVG = " + serAVG + " = " + serendipityTot + "/" + nRec);
            serendipityAllUser.add(serAVG);

        }
        double avg = 0;
        for (Double aDouble : serendipityAllUser) {
            avg += aDouble;
        }
        avg = avg / serendipityAllUser.size();
        return avg;
    }

    private static UndirectedSparseMultigraph<String, Edge> graphFiltered() {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream("./serialized/graphComplete.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        UndirectedSparseMultigraph<String, Edge> recGraph = null;
        try {
            recGraph = (UndirectedSparseMultigraph<String, Edge>) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String dir;
        switch (LoadProperties.FILTERTYPE) {
            case "RankerWeka":
                dir = "./mapping/choosen_prop/choosen_prop" + LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + LoadProperties.EVALWEKA;
                break;
            case "CFSubsetEval":
                dir = "./mapping/choosen_prop/choosen_prop" + LoadProperties.FILTERTYPE;
                break;
            default:
                dir = "./mapping/choosen_prop/choosen_prop" + LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER;
                break;
        }
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(dir),
                    Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collection<Edge> edgesrecGraph = recGraph.getEdges();
        List<Edge> edgesrecGraphRemove = new ArrayList<>();
//        System.out.println("Edges init: " + edgesrecGraph.size());
        for (Edge edge : edgesrecGraph) {
            if (!lines.contains(edge.getProperty())) {
                edgesrecGraphRemove.add(edge);
            }
        }
        for (Edge edge : edgesrecGraphRemove) {
            recGraph.removeEdge(edge);
        }
//        System.out.println("Edges post: " + recGraph.getEdges().size());
        return recGraph;


        /* Load  bin graph complete with user */
//        String nameF = LoadProperties.FILTERTYPE;
//        if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
//            nameF += LoadProperties.EVALWEKA;
//        nameF += LoadProperties.NUMFILTER;
//        UndirectedSparseMultigraph<String, Edge> recGraph = new UndirectedSparseMultigraph<>();
//        for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
//
//            FileInputStream fis = null;
//            try {
//                fis = new FileInputStream("./serialized/graph" + nameF + "Split" + i + ".bin");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            ObjectInputStream ois = null;
//            try {
//                ois = new ObjectInputStream(fis);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            UndirectedSparseMultigraph<String, Edge> recGraphTemp = null;
//            try {
//                recGraphTemp = (UndirectedSparseMultigraph<String, Edge>) ois.readObject();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            for (String s : recGraphTemp.getVertices()) {
//                recGraph.addVertex(s);
//            }
//            for (Edge edge : recGraphTemp.getEdges()) {
//                recGraph.addEdge(edge, edge.getSubject(), edge.getObject());
//            }

//            System.out.println("Split" + i + ": " + recGraph.getEdgeCount() + " " + recGraph.getVertexCount());
//        }
//        return recGraph;
    }

    private static double shortestPathMetric(String film1, String film2, UndirectedSparseMultigraph<String, Edge> recGraph) {

        assert recGraph != null;
        try {

            DijkstraShortestPath<String, Edge> shortestPath = new DijkstraShortestPath<>(recGraph);
            List<Edge> edges = shortestPath.getPath(film1, film2);
            return edges.size();
        } catch (Exception e) {
            return 0.0;
        }
    }


    public static void saveAllTrecEvalResult(String goldStandardFile, String resultFile, String trecResultFile) {

        String[] measures = new String[]{"P.5", "P.10", "P.15", "P.20", "P.30", "P.50", "recall.5", "recall.10", "recall.15", "recall.20", "recall.30", "recall.50"};

        for (String measure : measures) {
            String resTemp = trecResultFile + "Temp";
            String trecEvalCommand = PATHTREC + "trec_eval -q -m " + measure + " " + goldStandardFile + " " + resultFile;
            CmdExecutor.executeCommandAndPrint(trecEvalCommand, resTemp);
            CmdExecutor.executeCommand("cat " + resTemp + " >> " + trecResultFile, false);
            new File(resTemp).delete();
        }
    }

    /**
     * Parses the specified trec eval results file and retrives all the produced
     * metrics
     *
     * @param trecEvalFile filename of the trec_eval results file
     * @return a dictionary whose keys are metrics' name and whose values are metrics' values
     * @throws IOException
     */
    public static Map<String, HashMap<String, Float>> getAllTrecEvalResults(String trecEvalFile) throws IOException {
        CSVParser parser = null;
        Map<String, HashMap<String, Float>> trecMetrics = new HashMap<>();

        try {
            // trec_eval loading
            if (new File(trecEvalFile).exists()) {
                parser = new CSVParser(new FileReader(trecEvalFile), CSVFormat.TDF);
                for (CSVRecord rec : parser.getRecords()) {
                    if (rec.size() == 3) {
                        if (!rec.get(1).equals("all"))
                            if (trecMetrics.get(rec.get(1)) == null) {
                                trecMetrics.put(rec.get(1), new HashMap<String, Float>(6));
                                trecMetrics.get(rec.get(1)).put(rec.get(0), Float.parseFloat(rec.get(2)));
                            } else {
                                trecMetrics.get(rec.get(1)).put(rec.get(0), Float.parseFloat(rec.get(2)));
                            }
                    }
                }

            }
            // Diversity loading
            if (new File(trecEvalFile + "3").exists()) {
                parser = new CSVParser(new FileReader(trecEvalFile + "3"), CSVFormat.newFormat(','));
//            logger.info("Loading trec eval metrics from: " + trecEvalFile);
                for (CSVRecord rec : parser.getRecords()) {
                    trecMetrics.get(rec.get(0)).put(rec.get(1), Float.parseFloat(rec.get(2)));
                }
            }

            // Novelty loading
            if (new File(trecEvalFile + "4").exists()) {
                parser = new CSVParser(new FileReader(trecEvalFile + "4"), CSVFormat.newFormat(','));
//            logger.info("Loading trec eval metrics from: " + trecEvalFile);
                for (CSVRecord rec : parser.getRecords()) {
                    trecMetrics.get(rec.get(0)).put(rec.get(1), Float.parseFloat(rec.get(2)));
                }
            }

            return trecMetrics;

        } catch (IOException e) {
            throw e;
        } finally {
            assert parser != null;
            parser.close();
        }

    }

}
