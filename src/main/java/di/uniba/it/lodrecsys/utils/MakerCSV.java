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
    public static int[] F = new int[]{5, 10, 15, 20};


    private static HashMap<Integer, ArrayList<String>> loadtop(int top, String alg) throws IOException {
        HashMap<Integer, ArrayList<String>> map = new HashMap<>(5);
        String pathReader = new StringBuilder().append("./datasets/ml-100k/results/UserItemExpDBPedia/").append(alg).append(TOP[top]).append("prop").append("5split/summaries/FSum").toString();
        BufferedReader brFsum = new BufferedReader(new FileReader(pathReader));
        String line;
        int sparsityfile = 0;
        ArrayList<String> val = new ArrayList<>(6);
        while ((line = brFsum.readLine()) != null && sparsityfile < F.length) {
            if (line.equals(" ")) {
                map.put(F[sparsityfile], val);
                val = new ArrayList<>(6);
                sparsityfile++;
            } else
                val.add(line.replace(",", "."));
        }
        brFsum.close();

        return map;
    }


    private static HashMap<Integer, ArrayList<String>> loadbaseline() {
        HashMap<Integer, ArrayList<String>> map = new HashMap<>(6);
        ArrayList<String> val = new ArrayList<>(
                Arrays.asList("0.4596", "0.4622", "0.4722", "0.4765", "0.4803", "0.4973"));
        map.put(F[0], val);
        val = new ArrayList<>(
                Arrays.asList("0.5392", "0.5431", "0.5509", "0.5548", "0.5590", "0.5747"));
        map.put(F[1], val);
        val = new ArrayList<>(
                Arrays.asList("0.5381", "0.5418", "0.5482", "0.5519", "0.5566", "0.5702"));
        map.put(F[2], val);
        val = new ArrayList<>(
                Arrays.asList("0.5197", "0.5228", "0.5272", "0.5301", "0.5342", "0.5468"));
        map.put(F[3], val);
        return map;
    }

    /*
    @args PageRank
    Parameter 2 : Boolean(baseline yes no)
     */
    public static void main(String[] args) throws IOException {
        String alg = args[0];
        boolean base;
        if (args.length == 2)
            if (args[1].contains("baseline"))
                base = true;
            else
                base = false;
        else base = false;

        FileUtils.deleteDirectory(new File("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/" + alg + "/"));
        new File("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/" + alg + "/").mkdirs();

        HashMap<Integer, ArrayList<String>> baseline = loadbaseline();

        HashMap<Integer, ArrayList<String>> top10 = loadtop(0, alg);
        HashMap<Integer, ArrayList<String>> top17 = loadtop(1, alg);
        HashMap<Integer, ArrayList<String>> top30 = loadtop(2, alg);
        HashMap<Integer, ArrayList<String>> top50 = loadtop(3, alg);

        for (int i1 = 0; i1 < F.length; i1++) {
            String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/CSV/" + alg + "/F" + F[i1] + ".csv";
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

        for (int sparsity = 0; sparsity < F.length; sparsity++) {

            String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/CSV/" + alg + "/F" + F[sparsity] + ".csv";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));

            ArrayList<String> baseStrings = baseline.get(F[sparsity]);
            ArrayList<String> sparsitytop10 = top10.get(F[sparsity]);
            ArrayList<String> sparsitytop17 = top17.get(F[sparsity]);
            ArrayList<String> sparsitytop30 = top30.get(F[sparsity]);
            ArrayList<String> sparsitytop50 = top50.get(F[sparsity]);

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