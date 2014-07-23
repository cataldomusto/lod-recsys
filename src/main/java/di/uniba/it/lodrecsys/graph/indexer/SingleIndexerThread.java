package di.uniba.it.lodrecsys.graph.indexer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by asuglia on 7/23/14.
 */
public class SingleIndexerThread implements Callable<Pair<Multimap<String, String>, Map<String, Map<String, String>>>> {
    private PropertiesManager propertiesManager;
    private List<MovieMapping> itemsSet;

    public SingleIndexerThread(List<MovieMapping> itemsSet, PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
        this.itemsSet = itemsSet;
    }

    @Override
    public Pair<Multimap<String, String>, Map<String, Map<String, String>>> call() throws Exception {
        Multimap<String, String> propIndexer = HashMultimap.create();
        Map<String, Map<String, String>> itemRepresentation = new HashMap<>();

        // loop for each item
        for (MovieMapping movie : itemsSet) {
            List<Statement> propList = propertiesManager.getResourceProperties(movie.getDbpediaURI());
            Map<String, String> currItemProp = new HashMap<>();

            for (Statement stat : propList) {
                String propValue = stat.getObject().toString();
                propIndexer.put(propValue, movie.getItemID());
                currItemProp.put(stat.getPredicate().toString(), propValue);
            }

            itemRepresentation.put(movie.getItemID(), currItemProp);
        }


        return new Pair<>(propIndexer, itemRepresentation);
    }
}
