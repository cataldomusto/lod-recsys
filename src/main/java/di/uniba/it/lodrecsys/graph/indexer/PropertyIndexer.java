package di.uniba.it.lodrecsys.graph.indexer;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by asuglia on 7/22/14.
 */
public class PropertyIndexer {
    private Map<String, Set<String>> invertedIndex;

    public PropertyIndexer(String dbpediaMappingFile, String propertyIndexDir) throws IOException, ExecutionException, InterruptedException {

        invertedIndex = new HashMap<>();
        indexItems(Utils.loadDBpediaMappedItems(dbpediaMappingFile), new PropertiesManager(propertyIndexDir), 5);
    }

    private Map<String, String> getMapForMappedItems(List<MovieMapping> movieList) {
        // key: item-id - value: dbpedia uri
        Map<String, String> idUriMap = new HashMap<>();

        for (MovieMapping movie : movieList) {
            idUriMap.put(movie.getItemID(), movie.getDbpediaURI());

        }

        return idUriMap;
    }

    private void indexItems(List<MovieMapping> itemsSet, PropertiesManager propertiesManager, int numThread) throws ExecutionException, InterruptedException {
        int itemsPerThread = itemsSet.size() / numThread;

        ExecutorService threadExecutor = Executors.newCachedThreadPool();

        List<List<MovieMapping>> itemSubLists = com.google.common.collect.Lists.partition(itemsSet, itemsPerThread);

        List<Future<Map<String, Set<String>>>> computedIndex = new ArrayList<>();


        for (int i = 0; i < numThread; i++)
            computedIndex.add(threadExecutor.submit(new SingleIndexerThread(itemSubLists.get(i), propertiesManager)));

        for (Future<Map<String, Set<String>>> newIndexResult : computedIndex) {
            invertedIndex.putAll(newIndexResult.get());

        }

        threadExecutor.shutdown();
    }

}
