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


//                Execute all algorithm of feature selection
                GraphFactory.createAllFeatureSelection(trainFile, testFile, LoadProperties.PROPERTYINDEXDIR, mappingList);


//                Create Graph to filter
//                GraphFactory.createSubsetFeature(LoadProperties.FILTERTYPE, trainFile, testFile, LoadProperties.PROPERTYINDEXDIR, mappingList);

                //Copy n-properties to graph
                GraphFactory.subsetProp();

//               Create Graph with subset of feature
                Pair<RecGraph, RequestStruct> pair = GraphFactory.create(LoadProperties.METHOD, trainFile,
                        testFile, LoadProperties.MASSPROB, LoadProperties.PROPERTYINDEXDIR, mappingList, tagmeConcepts);
                RecGraph userItemGraph = pair.key;
                RequestStruct requestStruct = pair.value;

                recommendationForSplits.add(userItemGraph.runPageRank(requestStruct));
                LOGGERGRAPHRUNNER.info("Computed recommendations for split #" + i + " level: " + level);
            }


            for (int numRec : LoadProperties.LISTRECSIZES) {
                String namePath;
                if (LoadProperties.FILTERTYPE.equals("RankerWeka"))
                    namePath = LoadProperties.RESPATH + File.separator +
                            LoadProperties.METHOD + File.separator +
                            LoadProperties.FILTERTYPE + LoadProperties.EVALWEKA + File.separator +
                            level + File.separator +
                            "top_" + numRec;
                else
                    namePath = LoadProperties.RESPATH + File.separator +
                            LoadProperties.METHOD + File.separator +
                            LoadProperties.FILTERTYPE + File.separator +
                            level + File.separator +
                            "top_" + numRec;

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
        }

    }
}
