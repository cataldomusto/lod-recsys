package di.uniba.it.lodrecsys;

import di.uniba.it.lodrecsys.baseline.IIRecSys;
import di.uniba.it.lodrecsys.baseline.UURecSys;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.ExperimentFactory;
import di.uniba.it.lodrecsys.eval.NumRec;
import org.apache.commons.math3.analysis.function.Exp;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveArrayIterator;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by asuglia on 4/4/14.
 */
public class Main {

    private static Logger currLogger = Logger.getLogger(Main.class.getName());


    public static void main(String[] args) throws IOException, IllegalAccessException, TasteException, InstantiationException, InterruptedException {
        /**
         * THE STARTING POINT OF EACH OPERATION
         *
         */

        DataModel testModel = new FileDataModel(new File(String.valueOf(Main.class.getResource("/binarized/u1.test").getFile())));
        DataModel model = new FileDataModel(new File(String.valueOf(Main.class.getResource("/binarized/u1.base").getFile())));

        Recommender recommender = ExperimentFactory.generateExperiment(UURecSys.class, model);
        EvaluateRecommendation eval = new EvaluateRecommendation(recommender, model, NumRec.TEN_REC);

        eval.generateTrecEvalFile("/home/asuglia/thesis_data/dataset/movielens_100k/trec/u1.res");

        eval.saveTrecEvalResult("/home/asuglia/thesis_data/dataset/movielens_100k/trec/u1.test",
                "/home/asuglia/thesis_data/dataset/movielens_100k/trec/u1.res", 1);



    }

}
