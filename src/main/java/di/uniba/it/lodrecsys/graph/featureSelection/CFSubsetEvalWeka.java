package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.graph.VertexScored;
import di.uniba.it.lodrecsys.utils.GraphToMatrix;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static di.uniba.it.lodrecsys.graph.GraphRecRun.savefileLog;

/**
 * Created by simo on 24/12/14.
 */
public class CFSubsetEvalWeka extends FS {

    public CFSubsetEvalWeka(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) throws IOException {
        super(trainingFileName, testFile, proprIndexDir, mappedItems);
    }

    public void run() throws IOException {
        savefileLog(new Date() + " [INFO] Feature Selection with Weka CFSubsetEval inizialized.");
        new File(LoadProperties.MAPPINGPATH + "/FS").mkdirs();
        FileOutputStream fout = new FileOutputStream(LoadProperties.MAPPINGPATH + "/FS/CFSubsetEval");
        PrintWriter out = new PrintWriter(fout);

        GraphToMatrix.convertARFFADJ(recGraph);

        BufferedReader reader =
                new BufferedReader(new FileReader(LoadProperties.DATASETPATH + "/serialized/graphAD.arff"));
        ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
        Instances data = arff.getData();
        data.setClassIndex(0);

        AttributeSelection attributeSelection = new AttributeSelection();
        ASEvaluation eval = new CfsSubsetEval();
        BestFirst bestFirst = new BestFirst();
        try {
            bestFirst.setSearchTermination(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        attributeSelection.setEvaluator(eval);
        attributeSelection.setSearch(bestFirst);

        try {
            attributeSelection.SelectAttributes(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] s = new int[0];
        try {
            s = attributeSelection.selectedAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<VertexScored> rank = new ArrayList<>(s.length);
        for (int value : s) {
            if (value != 0) {
                VertexScored scored = new VertexScored(data.attribute(value).toString().split(" ")[1], 1);
                rank.add(scored);
            }
        }

        for (VertexScored vertexScored : rank)
            out.println(vertexScored.getScore() + " " + vertexScored.getProperty());

        out.close();
        fout.close();

        savefileLog(new Date() + " [INFO] Feature Selection with Weka Completed.");
        savefileLog("----------------------------------------------------");
    }

}
