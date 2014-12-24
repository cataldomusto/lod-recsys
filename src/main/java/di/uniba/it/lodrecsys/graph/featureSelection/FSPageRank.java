package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.graph.Edge;
import di.uniba.it.lodrecsys.graph.VertexPageRank;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by simo on 24/12/14.
 */
public class FSPageRank extends FS {

    public FSPageRank(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) throws IOException {
        super(trainingFileName, testFile, proprIndexDir, mappedItems);
    }

    public void run() throws IOException {
        FileOutputStream fout = new FileOutputStream("./rank");
        PrintWriter out = new PrintWriter(fout);

        edu.uci.ics.jung.algorithms.scoring.PageRank<String, Edge> pr = new edu.uci.ics.jung.algorithms.scoring.PageRank<>(recGraph, 0.15);
        pr.evaluate();

        Set<VertexPageRank> sortedVerticesSet =
                new TreeSet<>();

        for (String vert : recGraph.getVertices()) {
            VertexPageRank ver = new VertexPageRank(vert, pr.getVertexScore(vert));
            sortedVerticesSet.add(ver);
        }

        for (VertexPageRank vertexPageRank : sortedVerticesSet) {
            out.println(vertexPageRank.getResourceURI() + "  " + vertexPageRank.getScore());
        }

        out.close();
        fout.close();
    }

}
