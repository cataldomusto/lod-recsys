package di.uniba.it.lodrecsys.properties;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by asuglia on 7/30/14.
 */
public class JaccardSimilarityFunction implements SimilarityFunction {
    @Override
    public double compute(Multimap<String, String> first, Multimap<String, String> second) {
        Set<String> firstElements = new TreeSet<>(),
                secElements = new TreeSet<>();

        for (String firstKey : first.keySet()) {
            for (String propValue : first.get(firstKey)) {
                firstElements.add(firstKey + ":" + propValue);
            }
        }

        for (String secKey : second.keySet()) {
            for (String propValue : second.get(secKey)) {
                secElements.add(secKey + ":" + propValue);
            }
        }


        return (double) Sets.intersection(firstElements, secElements).size() / (double) Sets.union(firstElements, secElements).size();
    }
}
