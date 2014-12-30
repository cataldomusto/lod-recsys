package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.graph.Edge;
import di.uniba.it.lodrecsys.graph.VertexScored;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by simo on 24/12/14.
 */
public class PageRank extends FS {

    public PageRank(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) throws IOException {
        super(trainingFileName, testFile, proprIndexDir, mappedItems);
    }

    public void run() throws IOException {
        System.out.println(new Date() + " [INFO] Feature Selection with PageRank inizialized.");
        new File("./mapping/FS").mkdirs();
        FileOutputStream fout = new FileOutputStream("./mapping/FS/PageRank");
        PrintWriter out = new PrintWriter(fout);

        // Compute PageRank
        edu.uci.ics.jung.algorithms.scoring.PageRank<String, Edge> pr = new edu.uci.ics.jung.algorithms.scoring.PageRank<>(recGraph, 0.15);
        pr.evaluate();

        Set<VertexScored> sortedVerticesSet =
                new TreeSet<>();

        //Locate Object of triple and put into TreeSet
        for (String vert : recGraph.getVertices()) {
            Collection<Edge> inEd = recGraph.getIncidentEdges(vert);
            for (Edge edge : inEd)
                if (edge.getObject().equals(vert)) {
                    VertexScored ver = new VertexScored(edge.getProperty(), pr.getVertexScore(vert));
                    sortedVerticesSet.add(ver);
                }
        }

        //Insert VertexPageRank (property, score) into Array
        ArrayList<VertexScored> arrayList = new ArrayList<>(15);
        for (VertexScored vertexScored : sortedVerticesSet) {
            if (!arrayList.contains(vertexScored))
                arrayList.add(vertexScored);
        }

        for (VertexScored vertexScored : arrayList)
            out.println(vertexScored.getScore() + " " + vertexScored.getProperty());

        out.close();
        fout.close();

        System.out.println(new Date() + " [INFO] Feature Selection with PageRank Completed.");
        System.out.println("---------------------------------------------------");
    }

}
