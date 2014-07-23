package di.uniba.it.lodrecsys.graph.indexer;

import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

/**
 * Created by asuglia on 7/23/14.
 */
public class SingleIndexerThread implements Callable<Map<String, Set<String>>> {
    private PropertiesManager propertiesManager;
    private List<MovieMapping> itemsSet;

    public SingleIndexerThread(List<MovieMapping> itemsSet, PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
        this.itemsSet = itemsSet;
    }

    @Override
    public Map<String, Set<String>> call() throws Exception {
        Map<String, Set<String>> propIndexer = new HashMap<>();

        // loop for each item
        for (MovieMapping movie : itemsSet) {
            List<Statement> propList = propertiesManager.getResourceProperties(movie.getDbpediaURI());

            for (Statement stat : propList) {
                String propValue = stat.getObject().toString();
                Set<String> connectedItems = propIndexer.get(propValue);
                if (connectedItems == null)
                    connectedItems = new TreeSet<>();

                connectedItems.add(movie.getItemID());
                propIndexer.put(propValue, connectedItems);

            }


        }

        return propIndexer;
    }
}
