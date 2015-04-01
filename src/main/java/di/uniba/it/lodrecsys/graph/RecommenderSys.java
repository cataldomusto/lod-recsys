package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import static di.uniba.it.lodrecsys.eval.EvaluateRecommendation.mapFilmCount;
import static di.uniba.it.lodrecsys.graph.GraphRecRun.cleanfileLog;

/**
 * Created by Simone Rutigliano on 31/12/14.
 */
public class RecommenderSys implements Serializable {

    private static Logger LOGGERGRAPHRUNNER = Logger.getLogger(GraphRunner.class.getName());
    private static List<Map<String, Set<Rating>>> recommendationForSplits = new ArrayList<>();
    private static List<Map<String, String>> metricsForSplit = new ArrayList<>();
    private static List<Map<String, HashMap<String, Float>>> metricsForSplitALL = new ArrayList<>();
    private static List<MovieMapping> mappingList;
    private static Map<String, List<String>> tagmeConcepts;

    public static void loadValue() throws IOException {
        cleanfileLog();
        mappingList = Utils.loadDBpediaMappedItems(LoadProperties.MAPPEDITEMFILE);
//        tagmeConcepts = Utils.loadTAGmeConceptsForItems(LoadProperties.TAGMEDIR);
    }

    public static void featureSelection(String trainFile, String testFile) throws IOException {

//      Execute all algorithm of feature selection
        GraphFactory.createAllFeatureSelection(trainFile, testFile, LoadProperties.PROPERTYINDEXDIR, mappingList);

//      Create Graph to filter
        GraphFactory.createSubsetFeature(LoadProperties.FILTERTYPE, trainFile, testFile, LoadProperties.PROPERTYINDEXDIR, mappingList);

        //Copy n-properties to graph
        GraphFactory.subsetProp();
    }

    public static void recommendations(String trainFile, String testFile) throws IOException {
        //               Create Graph with subset of feature
        Pair<RecGraph, RequestStruct> pair = GraphFactory.create(LoadProperties.METHOD, trainFile,
                testFile, LoadProperties.MASSPROB, LoadProperties.PROPERTYINDEXDIR, mappingList, tagmeConcepts);
        RecGraph userItemGraph = pair.key;
        RequestStruct requestStruct = pair.value;
        recommendationForSplits.add(userItemGraph.runPageRank(requestStruct));
    }

    public static void saveRec(String level) throws IOException {
        String dir;
        switch (LoadProperties.FILTERTYPE) {
            case "RankerWeka":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            case "CFSubsetEval":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            case "Custom":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            default:
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;

        }

        new File("./" + dir).mkdirs();
        FileOutputStream fos = new FileOutputStream("./" + dir + "/recommendationForSplits.bin");
        ObjectOutputStream o = new ObjectOutputStream(fos);
        o.writeObject(recommendationForSplits);
        o.close();
        fos.close();
        recommendationForSplits.clear();
    }

    public static void loadRec(String level) throws IOException, ClassNotFoundException {
        String dir;
        switch (LoadProperties.FILTERTYPE) {
            case "RankerWeka":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            case "CFSubsetEval":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            case "Custom":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            default:
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;

        }
        FileInputStream fis = new FileInputStream("./" + dir + "/recommendationForSplits.bin");
        ObjectInputStream ois = new ObjectInputStream(fis);
        recommendationForSplits = (List<Map<String, Set<Rating>>>) ois.readObject();
        ois.close();
        fis.close();
    }

    private static void delSerRec(String level) {
        String dir;
        switch (LoadProperties.FILTERTYPE) {
            case "RankerWeka":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            case "CFSubsetEval":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            case "Custom":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            default:
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;

        }
        File serRec = new File("./" + dir + "/recommendationForSplits.bin");
        if (serRec.exists())
            serRec.delete();
    }

    public static void evaluator(String level, boolean novelty, boolean diversity, boolean serendipity, boolean extract) {
        String dir;
        switch (LoadProperties.FILTERTYPE) {
            case "RankerWeka":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            case "CFSubsetEval":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            case "Custom":
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;
            default:
                dir = LoadProperties.RESPATH + File.separator +
                        LoadProperties.METHOD + File.separator +
                        LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                        level;
                break;

        }
        try {
            loadRec(level);

            // Extract measure
            if (extract){
                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    EvaluateRecommendation.extractRatingRec(recommendationForSplits.get(i - 1));
                }
            }

            // Diversity measure
            ArrayList<String> diversityMeasureAll = null, diversityMeasureAvg = null;
            if (diversity) {
                ArrayList<HashMap<String, HashMap<String, Integer>>> mapFilmCountProp = mapFilmCount();
                diversityMeasureAll = new ArrayList<>(LoadProperties.NUMSPLIT);
                diversityMeasureAvg = new ArrayList<>(LoadProperties.NUMSPLIT);
                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    HashMap<String, String> measures = EvaluateRecommendation.evalILDMeasure(recommendationForSplits.get(i - 1), mapFilmCountProp);
                    diversityMeasureAll.add(measures.get("all"));
                    diversityMeasureAvg.add(measures.get("avg"));
                }
            }

//          Novelty measure
            ArrayList<String> noveltyMeasureAll = null, noveltyMeasureAvg = null;
            if (novelty) {
                noveltyMeasureAll = new ArrayList<>(LoadProperties.NUMSPLIT);
                noveltyMeasureAvg = new ArrayList<>(LoadProperties.NUMSPLIT);
                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    HashMap<String, String> measures = EvaluateRecommendation.evalMSIMeasure(recommendationForSplits.get(i - 1));
//                    noveltyMeasure.add(EvaluateRecommendation.evalMSIMeasure(recommendationForSplits.get(i - 1)));
                    noveltyMeasureAll.add(measures.get("all"));
                    noveltyMeasureAvg.add(measures.get("avg"));
                }
            }

//          Serendipity measure
            ArrayList<String> serendipityMeasure = null;
            if (serendipity) {
                serendipityMeasure = new ArrayList<>(LoadProperties.NUMSPLIT);
                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    serendipityMeasure.add(EvaluateRecommendation.evalSerMeasure(recommendationForSplits.get(i - 1)));
                }
            }

            for (int numRec : LoadProperties.LISTRECSIZES) {
                String namePath = dir + File.separator + "top_" + numRec;

                File f = new File(namePath);
                f.mkdirs();
                String completeResFile = namePath + File.separator + "metrics.complete";
                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    String trecTestFile = LoadProperties.TESTTRECPATH + File.separator + "u" + i + ".test";
                    String resFile = namePath + File.separator + "u" + i + ".results";

                    EvaluateRecommendation.serializeRatings(recommendationForSplits.get(i - 1), resFile, numRec);

                    String trecResultFinal = resFile.substring(0, resFile.lastIndexOf(File.separator))
                            + File.separator + "u" + i + ".final";
                    EvaluateRecommendation.saveTrecEvalResult(trecTestFile, resFile, trecResultFinal);
                    EvaluateRecommendation.saveAllTrecEvalResult(trecTestFile, resFile, trecResultFinal + "ALL");
                    if (diversity) {
                        EvaluateRecommendation.saveEvalILDMeasure(diversityMeasureAvg.get(i - 1), trecResultFinal);
                        EvaluateRecommendation.saveEvalILDMeasure(diversityMeasureAll.get(i - 1), trecResultFinal + "ALL");
                    }
                    if (novelty) {
                        EvaluateRecommendation.saveEvalMSIMeasure(noveltyMeasureAvg.get(i - 1), trecResultFinal);
                        EvaluateRecommendation.saveEvalMSIMeasure(noveltyMeasureAll.get(i - 1), trecResultFinal + "ALL");
                    }
                    if (serendipity)
                        EvaluateRecommendation.saveEvalSerendipityMeasure(serendipityMeasure.get(i - 1), trecResultFinal);
//                    LOGGERGRAPHRUNNER.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                    metricsForSplitALL.add(EvaluateRecommendation.getAllTrecEvalResults(trecResultFinal + "ALL"));
                    metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                }

//                LOGGERGRAPHRUNNER.info(("Metrics results for sparsity level " + level + "\n"));
                EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, LoadProperties.NUMSPLIT), completeResFile);
                EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResultALL(metricsForSplitALL), completeResFile + "ALL");
                metricsForSplit.clear(); // evaluate for the next sparsity level
                metricsForSplitALL.clear();

            }

            recommendationForSplits.clear();
//            delSerRec(level);
        } catch (ClassNotFoundException e) {
        } catch (IOException e) {
            System.err.println("Evaluate not executed.");
            System.err.println(e.toString());
        }
    }
}
