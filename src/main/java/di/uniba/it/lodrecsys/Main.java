package di.uniba.it.lodrecsys;

import di.uniba.it.lodrecsys.baseline.IIRecSys;
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
        DataModel dataModel = new FileDataModel(new File(args[0]));
        Recommender currRecommender = ExperimentFactory.generateExperiment(IIRecSys.class, dataModel);
        EvaluateRecommendation evaluator = new EvaluateRecommendation(currRecommender, new File(args[1]), NumRec.FIVE_REC);

    }

}
