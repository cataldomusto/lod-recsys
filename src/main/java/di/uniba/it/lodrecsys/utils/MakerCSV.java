package di.uniba.it.lodrecsys.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Simone Rutigliano on 21/01/15.
 */
public class MakerCSV {

    private static List<String> loadalg(String nFeature, String sparsity, String top, String metric, String alg) throws IOException {
        String fileName;
        if (alg.contains("CFSubsetEval"))
            fileName = "./datasets/ml-100k/results/UserItemExpDBPedia/" + alg + "5split/summaries/result" + metric + "_Top_" + top + sparsity + ".ALL";
        else
            fileName = "./datasets/ml-100k/results/UserItemExpDBPedia/" + alg + nFeature + "prop" + "5split/summaries/result" + metric + "_Top_" + top + sparsity + ".ALL";
        //        List<String> values = new ArrayList<>(lines.size());
//        for (String line : lines)
//            values.add(line.replace(",", "."));
        return Files.readAllLines(Paths.get(fileName),
                Charset.defaultCharset());

    }

    public static void main(String[] args) throws IOException {
        if (args[0].equals("comparisonAlg")) {
            ArrayList<String> algorithms = new ArrayList<>(9);
            algorithms.addAll(Arrays.asList(args).subList(5, args.length));
            comparisonAlg(args[1], args[2], args[3], args[4], algorithms);
        }

        if (args[0].equals("comparisonFeatures")) {
            ArrayList<String> tops = new ArrayList<>(4);
            for (int i = 5; i < args.length; i++)
                tops.add("Features" + args[i]);
            comparisonFeatures(args[1], args[2], args[3], args[4], tops);
        }

        if (args[0].equals("comparisonBestBaseline")) {

            comparisonBestBaseline(args[1], args[2], args[3], args[4], args[5]);
        }

    }

    private static void comparisonBestBaseline(String nFeature, String sparsity, String top, String metric, String algorithm) throws IOException {
        HashMap<String, ArrayList<String>> mapAlgVal = new HashMap<>(9);
        ArrayList<String> valuesBaseline = (ArrayList<String>) loadalg("17", sparsity, top, metric, "Baseline");
        ArrayList<String> valueBest = (ArrayList<String>) loadalg(nFeature, sparsity, top, metric, algorithm);
        mapAlgVal.put("Baseline17", valuesBaseline);
        String mapped;
        if (!algorithm.contains("CFSubsetEval"))
            mapped = algorithm + nFeature;
        else
            mapped = algorithm;
        mapAlgVal.put(mapped, valueBest);
        new File("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonBestBaseline/").mkdirs();

        String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonBestBaseline/confBaseline_" + algorithm + "_" + sparsity + "_" + top + "Top_" + metric + ".csv";
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));

        out.append("Baseline17," + mapped + "\n");

        int max = valuesBaseline.size();
        for (int i = 0; i < max; i++) {
            out.append(mapAlgVal.get("Baseline17").get(i)).append(",").append(mapAlgVal.get(mapped).get(i)).append("\n");
        }
        out.close();
    }

    private static void comparisonFeatures(String algorithm, String sparsity, String top, String metric, ArrayList<String> nFeatures) throws IOException {
        HashMap<String, ArrayList<String>> mapAlgVal = new HashMap<>(9);
        int max = 0;
        for (String nFeature : nFeatures) {
            ArrayList<String> values = (ArrayList<String>) loadalg(nFeature.replace("Features", ""), sparsity, top, metric, algorithm);
            mapAlgVal.put(nFeature, values);
            max = values.size();
        }

//        ArrayList<String> users = new ArrayList<>(max);
//        for (int i = 1; i <= max; i++)
//            users.add(String.valueOf(i));
//        mapAlgVal.put("Subject", users);
//        nFeatures.add("Subject");

//        FileUtils.deleteDirectory(new File("./datasets/ml-100k/results/UserItemExpDBPedia/ComparisonAlg/CSV/"));
        new File("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonFeatures/").mkdirs();

        String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonFeatures/conf_" + algorithm + "_" + sparsity + "_" + top + "Top_" + metric + ".csv";
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));

        for (int i = 0; i < nFeatures.size() - 1; i++)
            out.append(nFeatures.get(i)).append(",");
        out.append(nFeatures.get(nFeatures.size() - 1)).append("\n");

        for (int i = 0; i < max; i++) {
            for (int j = 0; j < nFeatures.size() - 1; j++) {
                out.append(mapAlgVal.get(nFeatures.get(j)).get(i));
                out.append(",");
            }
            out.append(mapAlgVal.get(nFeatures.get(nFeatures.size() - 1)).get(i)).append("\n");
        }
        out.close();
//        System.out.println("Finished config: " + algorithm + "_" + sparsity + "_" + top + "Top_" + metric + ".csv");
    }

    private static void comparisonAlg(String nFeature, String sparsity, String top, String metric, ArrayList<String> algorithms) throws IOException {
        HashMap<String, ArrayList<String>> mapAlgVal = new HashMap<>(9);
        int max = 0;
        for (String algorithm : algorithms) {
            ArrayList<String> values = (ArrayList<String>) loadalg(nFeature, sparsity, top, metric, algorithm);
            mapAlgVal.put(algorithm, values);
            max = values.size();
        }

//        ArrayList<String> users = new ArrayList<>(max);
//        for (int i = 1; i <= max; i++)
//            users.add(String.valueOf(i));
//        mapAlgVal.put("Subject", users);
//        algorithms.add("Subject");


//        FileUtils.deleteDirectory(new File("./datasets/ml-100k/results/UserItemExpDBPedia/ComparisonAlg/CSV/"));
        new File("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonAlg/").mkdirs();

        String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonAlg/conf_" + nFeature + "Features_" + sparsity + "_" + top + "Top_" + metric + ".csv";
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));

        for (int i = 0; i < algorithms.size() - 1; i++)
            out.append(algorithms.get(i)).append(",");
        out.append(algorithms.get(algorithms.size() - 1)).append("\n");

        for (int i = 0; i < max; i++) {
            for (int j = 0; j < algorithms.size() - 1; j++) {
                out.append(mapAlgVal.get(algorithms.get(j)).get(i));
                out.append(",");
            }
            out.append(mapAlgVal.get(algorithms.get(algorithms.size() - 1)).get(i)).append("\n");
        }
        out.close();
//        System.out.println("Finished config: " + nFeature + "Features_" + sparsity + "_" + top + "Top_" + metric + ".csv");
    }

    private static HashMap<Integer, ArrayList<String>> loadbaselineF1Sum() {
        int[] MetricsLevelF1 = new int[]{5, 10, 15, 20};
        HashMap<Integer, ArrayList<String>> map = new HashMap<>(6);
        ArrayList<String> val = new ArrayList<>(
                Arrays.asList("0.4596", "0.4622", "0.4722", "0.4765", "0.4803", "0.4973"));
        map.put(MetricsLevelF1[0], val);
        val = new ArrayList<>(
                Arrays.asList("0.5392", "0.5431", "0.5509", "0.5548", "0.5590", "0.5747"));
        map.put(MetricsLevelF1[1], val);
        val = new ArrayList<>(
                Arrays.asList("0.5381", "0.5418", "0.5482", "0.5519", "0.5566", "0.5702"));
        map.put(MetricsLevelF1[2], val);
        val = new ArrayList<>(
                Arrays.asList("0.5197", "0.5228", "0.5272", "0.5301", "0.5342", "0.5468"));
        map.put(MetricsLevelF1[3], val);
        return map;
    }

    private static void createCSVSum(String[] args) throws IOException {
        String alg = args[0];
        String metric = args[1];

        if (metric.contains("alpha-nDCG") || metric.contains("P-IA"))
            MetricsLevel = new int[]{5, 10, 20};
        else
            MetricsLevel = new int[]{5, 10, 15, 20};

        boolean base;
        if (args.length == 3)
            if (metric.contains("F1") && args[2].contains("baseline"))
                base = true;
            else
                base = false;
        else base = false;

        new File("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/" + metric + "/" + alg + "/").mkdirs();

        String all = "";
        for (String arg : args) {
            if (arg.contains("all"))
                all = "ALL";
        }
        HashMap<Integer, ArrayList<String>> baseline = loadbaselineF1Sum();

        HashMap<Integer, ArrayList<String>> top10 = loadtopSum(0, alg, metric, all);
        HashMap<Integer, ArrayList<String>> top17 = loadtopSum(1, alg, metric, all);
        HashMap<Integer, ArrayList<String>> top30 = loadtopSum(2, alg, metric, all);
        HashMap<Integer, ArrayList<String>> top50 = loadtopSum(3, alg, metric, all);


        for (int i1 = 0; i1 < MetricsLevel.length; i1++) {
            String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/CSV/" + metric + "/" + alg + "/MetricsLevel" + MetricsLevel[i1] + all + ".csv";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));

            if (base)
                out.append("baseline,");

            for (int i = 0; i < TOP.length - 1; i++) {
                out.append(alg + TOP[i] + ",");
            }
            out.append(alg + TOP[TOP.length - 1]);
            out.append("\n");
            out.close();
        }

        for (int sparsity = 0; sparsity < MetricsLevel.length; sparsity++) {

            String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/CSV/" + metric + "/" + alg + "/MetricsLevel" + MetricsLevel[sparsity] + all + ".csv";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));

            ArrayList<String> baseStrings = baseline.get(MetricsLevel[sparsity]);
            ArrayList<String> sparsitytop10 = top10.get(MetricsLevel[sparsity]);
            ArrayList<String> sparsitytop17 = top17.get(MetricsLevel[sparsity]);
            ArrayList<String> sparsitytop30 = top30.get(MetricsLevel[sparsity]);
            ArrayList<String> sparsitytop50 = top50.get(MetricsLevel[sparsity]);

            for (int i = 0; i < sparsitytop10.size(); i++) {
                if (!base)
                    out.append(sparsitytop10.get(i)).append(",").append(sparsitytop17.get(i)).append(",").append(sparsitytop30.get(i)).append(",").append(sparsitytop50.get(i)).append("\n");
                else
                    out.append(baseStrings.get(i)).append(",").append(sparsitytop10.get(i)).append(",").append(sparsitytop17.get(i)).append(",").append(sparsitytop30.get(i)).append(",").append(sparsitytop50.get(i)).append("\n");
            }
            out.close();
        }
        System.out.println("Finished " + alg);
    }

    private static int[] TOP = new int[]{10, 17, 30, 50};
    private static int[] MetricsLevel = new int[]{5, 10, 15, 20};

    private static HashMap<Integer, ArrayList<String>> loadtopSum(int top, String alg, String metric, String all) throws IOException {
        HashMap<Integer, ArrayList<String>> map = new HashMap<>(5);
        String pathReader = "./datasets/ml-100k/results/UserItemExpDBPedia/" + alg + TOP[top] + "prop" + "5split/summaries/" + metric + "Sum" + all;
        BufferedReader brFsum = new BufferedReader(new FileReader(pathReader));
        String line;
        int sparsityfile = 0;
        ArrayList<String> val = new ArrayList<>(5620);
        while ((line = brFsum.readLine()) != null && sparsityfile < MetricsLevel.length) {
            if (!line.contains(",")) {
                System.out.println(sparsityfile + " " + MetricsLevel.length);
                map.put(MetricsLevel[sparsityfile], val);
                val = new ArrayList<>(5620);
                sparsityfile++;
            } else
                val.add(line.replace(",", "."));
        }
        brFsum.close();

        return map;
    }

}