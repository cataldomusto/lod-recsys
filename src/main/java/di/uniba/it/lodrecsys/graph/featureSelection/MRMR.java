package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.graph.VertexScored;
import di.uniba.it.lodrecsys.utils.GraphToMatrix;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import en_deep.mlprocess.computation.mRMR;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by simo on 24/12/14.
 */
public class MRMR extends FS {

    public MRMR(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) throws IOException {
        super(trainingFileName, testFile, proprIndexDir, mappedItems);
    }

    public void run() throws IOException {
        new File("./mapping/FS").mkdirs();
        FileOutputStream fout = new FileOutputStream("./mapping/FS/MRMR");
        PrintWriter out = new PrintWriter(fout);

        GraphToMatrix.convertARFFADJ(recGraph);

        BufferedReader reader =
                new BufferedReader(new FileReader("./serialized/graphAD.arff"));
        ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
        Instances data = arff.getData();
        data.setClassIndex(0);

        en_deep.mlprocess.computation.mRMR a = new en_deep.mlprocess.computation.mRMR();
        a.setNumToSelect(-1);
        try {
            a.buildEvaluator(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        a.setGenerateRanking(true);
        double[][] s = new double[0][];
        try {
            s = a.rankedAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<VertexScored> rank = new ArrayList<>(s.length);
        for (double[] value : s) {
            VertexScored scored = new VertexScored(data.attribute((int) (value[0])).toString().split(" ")[1], value[1]);
            rank.add(scored);
        }

        for (VertexScored vertexScored : rank)
            out.println(vertexScored.getScore() + " " + vertexScored.getProperty());

        out.close();
        fout.close();

        System.out.println(new Date() + " [INFO] Feature Selection with mRMR Completed.");
    }

}
