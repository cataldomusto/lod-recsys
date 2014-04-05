package di.uniba.it.lodrecsys;

import di.uniba.it.lodrecsys.baseline.IIRecSys;
import di.uniba.it.lodrecsys.baseline.UURecSys;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.ExperimentFactory;
import di.uniba.it.lodrecsys.eval.NumRec;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.File;
import java.io.IOException;

/**
 * Created by asuglia on 4/4/14.
 */
public class Main {


    public static void main(String[] args) throws IOException, IllegalAccessException, TasteException, InstantiationException {
        /**
         * THE STARTING POINT OF EACH OPERATION
         *
         * */

        for (int i = 1; i <= 5; i++) {
            String trainSet = "/home/asuglia/thesis_data/dataset/movielens_100k/binarized/u" + i + ".base",
                    testSet = "/home/asuglia/thesis_data/dataset/movielens_100k/trec/u" + i + ".test",
                    resultSet = "/home/asuglia/thesis_data/dataset/movielens_100k/trec/u" + i + ".res";
            DataModel dataModel = new FileDataModel(new File(trainSet));
            Recommender currRecommender = ExperimentFactory.generateExperiment(UURecSys.class, dataModel);
            EvaluateRecommendation evaluator = new EvaluateRecommendation(currRecommender, new File(testSet), NumRec.TEN_REC);
            evaluator.generateTrecEvalFile(resultSet);
        }
    }

}
