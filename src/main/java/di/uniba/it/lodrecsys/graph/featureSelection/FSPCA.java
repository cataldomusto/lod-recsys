package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.graph.Edge;
import di.uniba.it.lodrecsys.graph.VertexScored;
import di.uniba.it.lodrecsys.utils.GraphToMatrix;
import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by simo on 24/12/14.
 */
public class FSPCA extends FS {

    public FSPCA(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) throws IOException {
        super(trainingFileName, testFile, proprIndexDir, mappedItems);
    }

    public void run() throws IOException {
        FileOutputStream fout1 = new FileOutputStream("./mapping/choosen_prop");
        PrintWriter out1 = new PrintWriter(fout1);

        new File("./mapping/FS").mkdirs();
        FileOutputStream fout = new FileOutputStream("./mapping/FS/FSPCA");
        PrintWriter out = new PrintWriter(fout);

        GraphToMatrix.convertARFF(recGraph);


        System.exit(1);

        Set<VertexScored> sortedVerticesSet =
                new TreeSet<>();

        //Locate Object of triple and put into TreeSet
        for (String vert : recGraph.getVertices()) {
            Collection<Edge> inEd = recGraph.getIncidentEdges(vert);
            for (Edge edge : inEd)
                if (edge.getObject().equals(vert)) {
                    VertexScored ver = new VertexScored(edge.getProperty(), 0.0);
                    sortedVerticesSet.add(ver);
                }
        }

        //Insert VertexScored
        // (property, score) into Array
        ArrayList<VertexScored> arrayList = new ArrayList<>(15);
        for (VertexScored vertexScored : sortedVerticesSet) {
            if (!arrayList.contains(vertexScored))
                arrayList.add(vertexScored);
        }

        //Select first NUMFILTER properties
        int i = 0;
        for (VertexScored vertexScored : arrayList) {
            out.println(vertexScored.getScore() + " " + vertexScored.getProperty());
            if (i < Integer.parseInt(LoadProperties.NUMFILTER)) {
                out1.println(vertexScored.getProperty());
                i++;
            }
        }

        out.close();
        fout.close();

        out1.close();
        fout1.close();

        System.out.println("[INFO] Feature Selection with PCA Completed.");
    }

}
