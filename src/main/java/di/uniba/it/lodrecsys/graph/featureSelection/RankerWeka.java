package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.graph.VertexScored;
import di.uniba.it.lodrecsys.utils.GraphToMatrix;
import weka.attributeSelection.*;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        System.out.println(new Date() + " [INFO] Feature Selection with Weka Ranker " + evalName + " inizialized.");
        new File("./mapping/FS").mkdirs();
        FileOutputStream fout = new FileOutputStream("./mapping/FS/RankerWeka" + evalName);
        PrintWriter out = new PrintWriter(fout);

        GraphToMatrix.convertARFFADJ(recGraph);

        BufferedReader reader =
                new BufferedReader(new FileReader("./serialized/graphAD.arff"));
        ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
        Instances data = arff.getData();
        data.setClassIndex(0);

        AttributeSelection attributeSelection = new AttributeSelection();
        ASEvaluation eval = null;
        switch (evalName) {
            case "InfoGainAttributeEval":
                eval = new InfoGainAttributeEval();
                ((InfoGainAttributeEval) eval).setBinarizeNumericAttributes(true);    // BinarizeNumericAttributes
                break;

            case "GainRatioAttributeEval":
                eval = new GainRatioAttributeEval();
                break;

            case "SVMAttributeEval":
                eval = new SVMAttributeEval();
                break;

            case "ReliefFAttributeEval":
                eval = new ReliefFAttributeEval();
                break;

            case "ChiSquaredAttributeEval":
                eval = new ChiSquaredAttributeEval();
                break;

            case "FilteredAttributeEval":
                eval = new FilteredAttributeEval();
                break;

            case "PCA":
                eval = new PrincipalComponents();
                break;

            case "OneRAttributeEval":
                eval = new OneRAttributeEval();
                break;

            case "LatentSemanticAnalysis":
                eval = new LatentSemanticAnalysis();
                break;

            case "SymmetricalUncertAttributeEval":
                eval = new SymmetricalUncertAttributeEval();
                break;
//            case "CorrelationAttributeEval":                   // Usage weka-dev 3.7.11
//                eval = new CorrelationAttributeEval();
//                break;
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

        for (VertexScored vertexScored : rank)
            out.println(vertexScored.getScore() + " " + vertexScored.getProperty());

        out.close();
        fout.close();


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

        System.out.println(new Date() + " [INFO] Feature Selection with Weka Ranker " + evalName + " Completed.");
        System.out.println("----------------------------------------------------");
    }

}
