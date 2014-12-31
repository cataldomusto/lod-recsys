package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.eval.SparsityLevel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static di.uniba.it.lodrecsys.graph.RecommenderSys.loadValue;

/**
 * Starts all the graph-based experiments and evaluate them
 * according to the trec_eval program.
 */
public class GraphRunner {

    public static void main(String[] args) throws IOException {

        loadValue();

        for (SparsityLevel level : SparsityLevel.values())
            new RecommenderSys(level.toString()).start();

    }

    public static void savefileLog(String s) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("./sperimentazione", true)))) {
            out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(s);
    }
}
