package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.graph.VertexScored;
import di.uniba.it.lodrecsys.utils.CmdExecutor;
import di.uniba.it.lodrecsys.utils.GraphToMatrix;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import weka.attributeSelection.*;
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
public class RankerWeka extends FS {
    private String evalName;

    public RankerWeka(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems, String evalWeka) throws IOException {
        super(trainingFileName, testFile, proprIndexDir, mappedItems);
        evalName = evalWeka;
    }

    public void run() throws IOException {
        savefileLog(new Date() + " [INFO] Feature Selection with Weka Ranker " + evalName + " inizialized.");
        new File(LoadProperties.MAPPINGPATH + "/FS").mkdirs();
        FileOutputStream fout = new FileOutputStream(LoadProperties.MAPPINGPATH + "/FS/RankerWeka" + evalName);
        PrintWriter out = new PrintWriter(fout);

        GraphToMatrix.convertARFFADJ(recGraph);

        BufferedReader reader =
                new BufferedReader(new FileReader(LoadProperties.DATASETPATH + "/serialized/graphAD.arff"));
        ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
        Instances data = arff.getData();
        data.setClassIndex(0);

        AttributeSelection attributeSelection = new AttributeSelection();
        ASEvaluation eval = null;
        switch (evalName) {

            case "ChiSquaredAttributeEval":
                eval = new ChiSquaredAttributeEval();
                break;

            case "InfoGainAttributeEval":
                eval = new InfoGainAttributeEval();
//                ((InfoGainAttributeEval) eval).setBinarizeNumericAttributes(true);    // BinarizeNumericAttributes
                break;

            case "GainRatioAttributeEval":
                eval = new GainRatioAttributeEval();
                break;

            case "LatentSemanticAnalysis":
                eval = new LatentSemanticAnalysis();
                ((LatentSemanticAnalysis) eval).setRank(0.99999);
                break;

            case "PCA":
                eval = new PrincipalComponents();
                ((PrincipalComponents) eval).setVarianceCovered(0.9999);
                break;

            case "ReliefFAttributeEval":
                eval = new ReliefFAttributeEval();
                break;

            case "SVMAttributeEval":
                eval = new SVMAttributeEval();
                break;

        }

        Ranker ranker = new Ranker();
        ranker.setNumToSelect(-1);
        ranker.setGenerateRanking(true);
        attributeSelection.setEvaluator(eval);
        attributeSelection.setSearch(ranker);

        try {
            attributeSelection.SelectAttributes(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        double[][] s = new double[0][];
        try {
            s = attributeSelection.rankedAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<VertexScored> rank = new ArrayList<>(s.length);
        for (double[] value : s) {
            VertexScored scored = new VertexScored(data.attribute((int) (value[0])).toString().split(" ")[1], value[1]);
            rank.add(scored);
        }

        for (VertexScored vertexScored : rank) {
            if (!vertexScored.getProperty().equals("class"))
                out.println(vertexScored.getScore() + " " + vertexScored.getProperty());
        }
        out.close();
        fout.close();

        if (evalName.equals("SVMAttributeEval")) {
            CmdExecutor.executeCommand("mv " + LoadProperties.MAPPINGPATH + "/FS/RankerWeka" + evalName + " " + LoadProperties.MAPPINGPATH + "/FS/RankerWeka" + evalName + "1", true);
            CmdExecutor.executeCommandAndPrint("cat " + LoadProperties.MAPPINGPATH + "/FS/RankerWeka" + evalName + "1 | sort -g",LoadProperties.MAPPINGPATH + "/FS/RankerWeka" + evalName);
        }

//        // obtain the attribute indices that were selected
//        int[] indices = new int[0];
//        try {
//            indices = attributeSelection.selectedAttributes();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        ArrayList<Integer> rank = new ArrayList<>(indices.length - 1);
//        for (int indice : indices) {
//            if (indice != 0)
//                rank.add(indice);
//        }
//
//        for (int indice : rank)
//            out.println(data.attribute(indice).toString().split(" ")[1]);
//
//        out.close();
//        fout.close();

        savefileLog(new Date() + " [INFO] Feature Selection with Weka Ranker " + evalName + " Completed.");
        savefileLog("----------------------------------------------------");
    }

}
