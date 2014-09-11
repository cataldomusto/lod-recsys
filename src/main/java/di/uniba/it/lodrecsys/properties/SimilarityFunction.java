package di.uniba.it.lodrecsys.properties;

import com.google.common.collect.Multimap;

/**
 * Defines a generic similarity function that could be
 * used to determine how much similar are two instances
 */
public interface SimilarityFunction {
    public float compute(Multimap<String, String> first, Multimap<String, String> second);

    public float getMaxValue();

    public float getMinValue();
}
