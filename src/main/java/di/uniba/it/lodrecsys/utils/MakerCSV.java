package di.uniba.it.lodrecsys.utils;

import java.io.*;
import java.util.ArrayList;
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

    /*
    @args PageRank
     */
    public static void main(String[] args) throws IOException {
        String alg = args[0];

        new File("./datasets/ml-100k/results/UserItemExpDBPedia/" + alg + "/CSV/").mkdirs();

        HashMap<Integer, ArrayList<String>> top10 = loadtop(0, alg);
        HashMap<Integer, ArrayList<String>> top17 = loadtop(1, alg);
        HashMap<Integer, ArrayList<String>> top30 = loadtop(2, alg);
        HashMap<Integer, ArrayList<String>> top50 = loadtop(3, alg);

        for (int i1 = 0; i1 < F.length; i1++) {
            String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/" + alg + "/CSV/F" + F[i1] + ".csv";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));
            for (int i = 0; i < TOP.length - 1; i++) {
                out.append(alg + TOP[i] + ",");
            }
            out.append(alg + TOP[TOP.length - 1]);
            out.append("\n");
            out.close();
        }

        for (int sparsity = 0; sparsity < F.length; sparsity++) {

            String pathWriter = "./datasets/ml-100k/results/UserItemExpDBPedia/" + alg + "/CSV/F" + F[sparsity] + ".csv";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));

            ArrayList<String> sparsitytop10 = top10.get(F[sparsity]);
            ArrayList<String> sparsitytop17 = top17.get(F[sparsity]);
            ArrayList<String> sparsitytop30 = top30.get(F[sparsity]);
            ArrayList<String> sparsitytop50 = top50.get(F[sparsity]);

            for (int i = 0; i < sparsitytop10.size(); i++) {
                out.append(sparsitytop10.get(i) + "," + sparsitytop17.get(i) + "," + sparsitytop30.get(i) + "," + sparsitytop50.get(i) + "\n");
            }
            out.close();
        }
        System.out.println("Finished "+ alg);
    }
}