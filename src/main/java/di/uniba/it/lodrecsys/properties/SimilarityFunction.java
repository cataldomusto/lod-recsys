package di.uniba.it.lodrecsys.properties;

import com.google.common.collect.Multimap;

/**
 * Created by asuglia on 7/30/14.
 */
public interface SimilarityFunction {
    public double compute(Multimap<String, String> first, Multimap<String, String> second);
}
