package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.graph.Edge;
import di.uniba.it.lodrecsys.graph.VertexPageRank;
import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by simo on 24/12/14.
 */
public class FSHITS_HUB extends FS {

    public FSHITS_HUB(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) throws IOException {
        super(trainingFileName, testFile, proprIndexDir, mappedItems);
    }

    public void run() throws IOException {
        FileOutputStream fout1 = new FileOutputStream("./mapping/choosen_prop");
        PrintWriter out1 = new PrintWriter(fout1);

        new File("./mapping/FS").mkdirs();
        FileOutputStream fout = new FileOutputStream("./mapping/FS/HITS_HUB");
        PrintWriter out = new PrintWriter(fout);

        // Compute HITS
        edu.uci.ics.jung.algorithms.scoring.HITS<String, Edge> pr = new edu.uci.ics.jung.algorithms.scoring.HITS<>(recGraph, 0.15);
        pr.evaluate();

        Set<VertexPageRank> sortedVerticesSet =
                new TreeSet<>();

        //Locate Object of triple and put into TreeSet
        for (String vert : recGraph.getVertices()) {
            Collection<Edge> inEd = recGraph.getIncidentEdges(vert);
            for (Edge edge : inEd)
                if (edge.getObject().equals(vert)) {
                    VertexPageRank ver = new VertexPageRank(edge.getProperty(), pr.getVertexScore(vert).hub);
                    sortedVerticesSet.add(ver);
                }
        }

        //Insert VertexPageRank (property, score) into Array
        ArrayList<VertexPageRank> arrayList = new ArrayList<>(15);
        for (VertexPageRank vertexPageRank : sortedVerticesSet) {
            if (!arrayList.contains(vertexPageRank))
                arrayList.add(vertexPageRank);
        }

        //Select first NUMFILTER properties
        int i = 0;
        for (VertexPageRank vertexPageRank : arrayList) {
            out.println(vertexPageRank.getScore() + " " + vertexPageRank.getProperty());
            if (i < Integer.parseInt(LoadProperties.NUMFILTER)) {
                out1.println(vertexPageRank.getProperty());
                i++;
            }
        }

        out.close();
        fout.close();

        out1.close();
        fout1.close();

        System.out.println("[INFO] Feature Selection with HITS score: hub Completed.");
    }

}
