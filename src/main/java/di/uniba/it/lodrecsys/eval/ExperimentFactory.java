package di.uniba.it.lodrecsys.eval;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * Created by asuglia on 4/4/14.
 */
public class ExperimentFactory {

    public static <T> Recommender generateExperiment(Class<T> recBuilderClass, DataModel dataModel) throws TasteException, InstantiationException, IllegalAccessException {
        return getCorrectBuilder(recBuilderClass).buildRecommender(dataModel);
    }

    private static <T> RecommenderBuilder getCorrectBuilder(Class<T> recBuilderClass) throws IllegalAccessException, InstantiationException {
        return (RecommenderBuilder) recBuilderClass.newInstance();
    }

}
