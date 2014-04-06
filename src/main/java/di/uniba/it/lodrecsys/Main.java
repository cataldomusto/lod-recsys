package di.uniba.it.lodrecsys;

import di.uniba.it.lodrecsys.baseline.IIRecSys;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.ExperimentFactory;
import di.uniba.it.lodrecsys.eval.NumRec;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import java.io.*;
/**
 * Created by asuglia on 4/4/14.
 */
public class Main {


    private static void executeCommand(String command, String resultFilename) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(resultFilename));
            writer.write(output.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert writer != null;
            writer.close();
        }
    }

    public static void main(String[] args) throws IOException, IllegalAccessException, TasteException, InstantiationException, InterruptedException {
        /**
         * THE STARTING POINT OF EACH OPERATION
         *
         */

        for (int i = 1; i <= 5; i++) {
            String trainSet = "/home/asuglia/thesis_data/dataset/movielens_100k/binarized/u" + i + ".base",
                    testSet = "/home/asuglia/thesis_data/dataset/movielens_100k/trec/u" + i + ".test",
                    resultSet = "/home/asuglia/thesis_data/dataset/movielens_100k/trec/u" + i + ".res";
            DataModel dataModel = new GenericBooleanPrefDataModel(GenericBooleanPrefDataModel.toDataMap(new FileDataModel(new File(trainSet))));
            Recommender currRecommender = ExperimentFactory.generateExperiment(IIRecSys.class, dataModel);
            EvaluateRecommendation evaluator = new EvaluateRecommendation(currRecommender, new File(testSet), NumRec.TEN_REC);
            evaluator.generateTrecEvalFile(resultSet);

            //String commandTest = "trec_eval u" + i + ".test u" + i + ".res";
            //executeCommand(commandTest, "u" + i + ".final");

        }

    }

}
