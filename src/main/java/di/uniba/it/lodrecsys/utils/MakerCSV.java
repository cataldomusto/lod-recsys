package di.uniba.it.lodrecsys.utils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by simo on 21/01/15.
 */
public class MakerCSV {
    public static int[] TOP = new int[]{10, 17, 30, 50};
    public static int[] MetricsLevel = new int[]{5, 10, 15, 20};

    private static HashMap<Integer, ArrayList<String>> loadtop(int top, String alg, String metric) throws IOException {
        HashMap<Integer, ArrayList<String>> map = new HashMap<>(5);
        String pathReader = "./datasets/ml-100k/results/UserItemExpDBPedia/" + alg + TOP[top] + "prop" + "5split/summaries/" + metric + "Sum";
        BufferedReader brFsum = new BufferedReader(new FileReader(pathReader));
        String line;
        int sparsityfile = 0;
        ArrayList<String> val = new ArrayList<>(6);
        while ((line = brFsum.readLine()) != null && sparsityfile < MetricsLevel.length) {
            if (line.equals(" ")) {
                map.put(MetricsLevel[sparsityfile], val);
                val = new ArrayList<>(6);
                sparsityfile++;
            } else
                val.add(line.replace(",", "."));
        }
        brFsum.close();

        return map;
    }


    private static HashMap<Integer, ArrayList<String>> loadbaselineF1() {
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

    /*
    @args PageRank
    Parameter 1 : F1 measure
    Parameter 2 : Boolean(baseline yes no)
     */
    public static void main(String[] args) throws IOException {
        String alg = args[0];
        String metric = args[1];

        if (metric.contains("alpha-nDCG"))
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

        FileUtils.deleteDirectory(new File("./datasets/ml-100k/results/UserItemExpDBPedia/CSV" + metric + "/" + alg + "/"));
        new File("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/" + metric + "/" + alg + "/").mkdirs();

        HashMap<Integer, ArrayList<String>> baseline = loadbaselineF1();

        HashMap<Integer, ArrayList<String>> top10 = loadtop(0, alg, metric);
        HashMap<Integer, ArrayList<String>> top17 = loadtop(1, alg, metric);
        HashMap<Integer, ArrayList<String>> top30 = loadtop(2, alg, metric);
        HashMap<Integer, ArrayList<String>> top50 = loadtop(3, alg, metric);

        for (int i1 = 0; i1 < MetricsLevel.length; i1++) {
            String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/CSV/" + metric + "/" + alg + "/MetricsLevel" + MetricsLevel[i1] + ".csv";
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

            String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/CSV/" + metric + "/" + alg + "/MetricsLevel" + MetricsLevel[sparsity] + ".csv";
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
}