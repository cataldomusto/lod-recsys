package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.GraphToMatrix;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import en_deep.mlprocess.computation.mRMR;
import weka.attributeSelection.*;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by simo on 24/12/14.
 */
public class FSRankerWeka extends FS {
    private String evalName;

    public FSRankerWeka(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems, String evalWeka) throws IOException {
        super(trainingFileName, testFile, proprIndexDir, mappedItems);
        evalName = evalWeka;
    }

    public void run() throws IOException {
        FileOutputStream fout1 = new FileOutputStream("./mapping/choosen_prop");
        PrintWriter out1 = new PrintWriter(fout1);

        new File("./mapping/FS").mkdirs();
        FileOutputStream fout = new FileOutputStream("./mapping/FS/FSRanker" + evalName);
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
        }
        Ranker ranker = new Ranker();
        ranker.setNumToSelect(-1);
        attributeSelection.setEvaluator(eval);
        attributeSelection.setSearch(ranker);

//        Map<Attribute, Double> infogainscores = new HashMap<>();
//        for (int i = 0; i < data.numAttributes(); i++) {
//            Attribute t_attr = data.attribute(i);
//            double infogain = 0;
//            try {
//                infogain = eval.evaluateAttribute(i);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            infogainscores.put(t_attr, infogain);
//        }
        try {
            attributeSelection.SelectAttributes(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // obtain the attribute indices that were selected
        int[] indices = new int[0];
        try {
            indices = attributeSelection.selectedAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Integer> rank = new ArrayList<>(indices.length - 1);
        for (int indice : indices) {
            if (indice != 0)
                rank.add(indice);
        }

        //Select first NUMFILTER properties
        int i = 0;
        for (int indice : rank) {
            out.println(data.attribute(indice).toString().split(" ")[1]);
            if (i < Integer.parseInt(LoadProperties.NUMFILTER)) {
                out1.println(data.attribute(indice).toString().split(" ")[1]);
                i++;
            }
        }

        out.close();
        fout.close();

        out1.close();
        fout1.close();

        System.out.println("[INFO] Feature Selection with Weka " + evalName + " Completed.");
    }

}
