package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utils.Utils;
import org.apache.mahout.cf.taste.common.TasteException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by asuglia on 5/21/14.
 */
public class GraphRunner {
    private static Logger currLogger = Logger.getLogger(GraphRunner.class.getName());

    public static void main(String[] args) throws IOException, TasteException {
        String trainPath = "/home/asuglia/thesis/dataset/ml-100k/definitive",
                testPath = "/home/asuglia/thesis/dataset/ml-100k/binarized",
                testTrecPath = "/home/asuglia/thesis/dataset/ml-100k/trec",
                resPath = "/home/asuglia/thesis/dataset/ml-100k/results",
                propertyIndexDir = "/home/asuglia/thesis/content_lodrecsys/movielens/stored_prop",
                tagmeDir = "/home/asuglia/thesis/content_lodrecsys/movielens/tagme",
                mappedItemFile = "mapping/item.mapping";

        List<MovieMapping> mappingList = Utils.loadDBpediaMappedItems(mappedItemFile);
        Map<String, List<String>> tagmeConcepts = Utils.loadTAGmeConceptsForItems(tagmeDir);
        List<Map<String, String>> metricsForSplit = new ArrayList<>();
        int[] listRecSizes = new int[]{5, 10, 15, 20};
        int numberOfSplit = 5;
        double massProb = 0.8;
        List<Map<String, Set<Rating>>> recommendationForSplits = new ArrayList<>();

        String method = "UserItemPropTag";

        for (SparsityLevel level : SparsityLevel.values()) {

            for (int i = 1; i <= numberOfSplit; i++) {

                String trainFile = trainPath + File.separator + "given_" + level.toString() + File.separator +
                        "u" + i + ".base",
                        testFile = testPath + File.separator + "u" + i + ".test";

                Pair<RecGraph, RequestStruct> pair = GraphFactory.create(method, trainFile,
                        testFile, massProb, propertyIndexDir, mappingList, tagmeConcepts);
                RecGraph userItemGraph = pair.key;
                RequestStruct requestStruct = pair.value;

                recommendationForSplits.add(userItemGraph.runPageRank(requestStruct));
                currLogger.info("Computed recommendations for split #" + i);
            }


            for (int numRec : listRecSizes) {
                String completeResFile = resPath + File.separator + method + File.separator + "given_" + level.toString() + File.separator +
                        "top_" + numRec + File.separator + "metrics.complete";
                for (int i = 1; i <= numberOfSplit; i++) {
                    String trecTestFile = testTrecPath + File.separator + "u" + i + ".test",
                            resFile = resPath + File.separator + method + File.separator + "given_" + level.toString() + File.separator +
                                    "top_" + numRec + File.separator + "u" + i + ".results";

                    EvaluateRecommendation.serializeRatings(recommendationForSplits.get(i - 1), resFile, numRec);


                    String trecResultFinal = resFile.substring(0, resFile.lastIndexOf(File.separator))
                            + File.separator + "u" + i + ".final";
                    EvaluateRecommendation.saveTrecEvalResult(trecTestFile, resFile, trecResultFinal);
                    metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                    currLogger.info(metricsForSplit.get(metricsForSplit.size() - 1).toString());
                }

                currLogger.info(("Metrics results for sparsity level " + level + "\n"));
                EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit), completeResFile);
                metricsForSplit.clear(); // evaluate for the next sparsity level


            }

            recommendationForSplits.clear();
        }

    }
}
