package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Starts all the graph-based experiments and evaluate them
 * according to the trec_eval program.
 */
public class GraphRunner {
    private static Logger LOGGERGRAPHRUNNER = Logger.getLogger(GraphRunner.class.getName());

    public static void main(String[] args) throws IOException {

        List<MovieMapping> mappingList = Utils.loadDBpediaMappedItems(LoadProperties.MAPPEDITEMFILE);
        Map<String, List<String>> tagmeConcepts = Utils.loadTAGmeConceptsForItems(LoadProperties.TAGMEDIR);
        List<Map<String, String>> metricsForSplit = new ArrayList<>();

        List<Map<String, Set<Rating>>> recommendationForSplits = new ArrayList<>();

        for (SparsityLevel level : SparsityLevel.values()) {

            for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {

                String trainFile = LoadProperties.TRAINPATH + File.separator + level + File.separator +
                        "u" + i + ".base",
                        testFile = LoadProperties.TESTPATH + File.separator + "u" + i + ".test";

                GraphFactory.createGraph(LoadProperties.FILTERTYPE, trainFile, testFile, LoadProperties.PROPERTYINDEXDIR, mappingList);

                Pair<RecGraph, RequestStruct> pair = GraphFactory.create(LoadProperties.METHOD, trainFile,
                        testFile, LoadProperties.MASSPROB, LoadProperties.PROPERTYINDEXDIR, mappingList, tagmeConcepts);
                RecGraph userItemGraph = pair.key;
                RequestStruct requestStruct = pair.value;

                recommendationForSplits.add(userItemGraph.runPageRank(requestStruct));
                LOGGERGRAPHRUNNER.info("Computed recommendations for split #" + i + " level: " + level);
            }


            for (int numRec : LoadProperties.LISTRECSIZES) {
                File f = new File(LoadProperties.RESPATH + File.separator + LoadProperties.METHOD + File.separator + level + File.separator +
                        "top_" + numRec);
                f.mkdirs();
                String completeResFile = LoadProperties.RESPATH + File.separator + LoadProperties.METHOD + File.separator + level + File.separator +
                        "top_" + numRec + File.separator + "metrics.complete";
                for (int i = 1; i <= LoadProperties.NUMSPLIT; i++) {
                    String trecTestFile = LoadProperties.TESTTRECPATH + File.separator + "u" + i + ".test";
                    String resFile = LoadProperties.RESPATH + File.separator + LoadProperties.METHOD + File.separator + level + File.separator +
                            "top_" + numRec + File.separator + "u" + i + ".results";

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
        }

    }
}
