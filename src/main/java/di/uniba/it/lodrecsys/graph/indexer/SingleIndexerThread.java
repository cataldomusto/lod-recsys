package di.uniba.it.lodrecsys.graph.indexer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by asuglia on 7/23/14.
 */
public class SingleIndexerThread implements Callable<Multimap<String, String>> {
    private PropertiesManager propertiesManager;
    private List<MovieMapping> itemsSet;

    public SingleIndexerThread(List<MovieMapping> itemsSet, PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
        this.itemsSet = itemsSet;
    }

    @Override
    public Multimap<String, String> call() throws Exception {
        Multimap<String, String> propIndexer = HashMultimap.create();

        // loop for each item
        for (MovieMapping movie : itemsSet) {
            List<Statement> propList = propertiesManager.getResourceProperties(movie.getDbpediaURI());

            for (Statement stat : propList) {
                String propValue = stat.getObject().toString();
                propIndexer.put(propValue, movie.getItemID());

            }


        }

        return propIndexer;
    }
}
