package di.uniba.it.lodrecsys.eval;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

/**
 * Created by asuglia on 8/4/14.
 */
public class EvalPierpaolo {

    public static void main(String[] args) throws IOException {
        String resDir = "/home/asuglia/exp_pier/exp_out",
                testTrecPath = "/home/asuglia/thesis/dataset/ml-100k/trec";

        Map<String, Multimap<String, String>> mapResFile = new HashMap<>();
        List<Map<String, String>> metricsForSplit = new ArrayList<>();
        int numberOfSplit = 5;
        int[] listRecSizes = new int[]{5, 10, 15, 20};

        for (SparsityLevel level : SparsityLevel.values()) {
            String sparsityLevel = "given_" + level.toString();
            mapResFile.put(sparsityLevel, loadResFilePerMethod(resDir + File.separator + sparsityLevel));

        }

        for (SparsityLevel level : SparsityLevel.values()) {
            String sparsityLevel = "given_" + level.toString();
            Multimap<String, String> sparsityLevelRes = mapResFile.get(sparsityLevel);
            for (String method : sparsityLevelRes.keySet()) {
                int i = 1;

                String completeResFile = resDir + File.separator + sparsityLevel + File.separator + "metric" + File.separator + method + ".final";
                for (String foldName : sparsityLevelRes.get(method)) {
                    Map<String, Set<Rating>> recList = Utils.loadRatingForEachUser(foldName);
                    String splitName = foldName.substring(foldName.lastIndexOf(File.separator) + 1, foldName.indexOf("."));
                    String trecTestFile = testTrecPath + File.separator + splitName + ".test",
                            resFile = foldName + ".trec_rec";
                    EvaluateRecommendation.serializeRatings(recList, resFile, -1);
                    String trecResultFinal = resFile.substring(0, resFile.lastIndexOf(".")) + ".trec_output";
                    EvaluateRecommendation.saveTrecEvalResult(trecTestFile, resFile, trecResultFinal);
                    metricsForSplit.add(EvaluateRecommendation.getTrecEvalResults(trecResultFinal));
                }


                EvaluateRecommendation.generateMetricsFile(EvaluateRecommendation.averageMetricsResult(metricsForSplit, numberOfSplit), completeResFile);
                metricsForSplit.clear(); // next method


            }
        }

    }


    private static Multimap<String, String> loadResFilePerMethod(String directory) {
        File dir = new File(directory);
        Multimap<String, String> mapResFile = ArrayListMultimap.create();

        File[] listFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });

        if (listFiles != null) {

            for (File currFile : listFiles) {
                String fileName = currFile.getName(),

                        methodName = fileName.substring(fileName.indexOf("_") + 1, fileName.length());

                mapResFile.put(methodName, currFile.getAbsolutePath());

            }
        }

        return mapResFile;
    }
}
