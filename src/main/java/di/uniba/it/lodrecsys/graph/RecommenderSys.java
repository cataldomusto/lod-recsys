package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static di.uniba.it.lodrecsys.graph.GraphRecRun.cleanfileLog;

/**
 * Created by simo on 31/12/14.
 */
public class RecommenderSys implements Serializable {

    private static Logger LOGGERGRAPHRUNNER = Logger.getLogger(GraphRunner.class.getName());
    private static List<Map<String, Set<Rating>>> recommendationForSplits = new ArrayList<>();
    private static List<Map<String, String>> metricsForSplit = new ArrayList<>();
    private static List<MovieMapping> mappingList;
    private static Map<String, List<String>> tagmeConcepts;

    public static void loadValue() throws IOException {
        cleanfileLog();
        mappingList = Utils.loadDBpediaMappedItems(LoadProperties.MAPPEDITEMFILE);
//        tagmeConcepts = Utils.loadTAGmeConceptsForItems(LoadProperties.TAGMEDIR);
    }

    public static void featureSelection(String trainFile, String testFile) throws IOException {

//      Execute all algorithm of feature selection
//        GraphFactory.createAllFeatureSelection(trainFile, testFile, LoadProperties.PROPERTYINDEXDIR, mappingList);

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
        if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                    level;
        else
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                    level;
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
        if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                    level;
        else
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                    level;
        FileInputStream fis = new FileInputStream("./" + dir + "/recommendationForSplits.bin");
        ObjectInputStream ois = new ObjectInputStream(fis);
        recommendationForSplits = (List<Map<String, Set<Rating>>>) ois.readObject();
        ois.close();
        fis.close();
    }

    private static void delSerRec(String level) {
        String dir;
        if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                    level;
        else
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                    level;
        File serRec = new File("./" + dir + "/recommendationForSplits.bin");
        if (serRec.exists())
            serRec.delete();
    }

    public static void evaluator(String level) {
        String dir;
        if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                    level;
        else
            dir = LoadProperties.RESPATH + File.separator +
                    LoadProperties.METHOD + File.separator +
                    LoadProperties.FILTERTYPE + LoadProperties.NUMFILTER + "prop" + LoadProperties.NUMSPLIT + "split" + File.separator +
                    level;
        try {
            loadRec(level);
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
                    metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                    LOGGERGRAPHRUNNER.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                }

                LOGGERGRAPHRUNNER.info(("Metrics results for sparsity level " + level + "\n"));
                EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, LoadProperties.NUMSPLIT), completeResFile);
                metricsForSplit.clear(); // evaluate for the next sparsity level

            }
            recommendationForSplits.clear();
//            delSerRec(level);
        } catch (ClassNotFoundException e) {
        } catch (IOException e) {
            System.err.println("Evaluate not executed.");
        }
    }
}
