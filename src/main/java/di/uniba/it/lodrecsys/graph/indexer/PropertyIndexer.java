package di.uniba.it.lodrecsys.graph.indexer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
    private Multimap<String, String> invertedIndex;

    public PropertyIndexer(String dbpediaMappingFile, String propertyIndexDir) throws IOException, ExecutionException, InterruptedException {

        invertedIndex = HashMultimap.create();
        indexItems(Utils.loadDBpediaMappedItems(dbpediaMappingFile), new PropertiesManager(propertyIndexDir), 5);
    }

    private void indexItems(List<MovieMapping> itemsSet, PropertiesManager propertiesManager, int numThread) throws ExecutionException, InterruptedException {
        int itemsPerThread = itemsSet.size() / numThread;

        ExecutorService threadExecutor = Executors.newCachedThreadPool();

        List<List<MovieMapping>> itemSubLists = com.google.common.collect.Lists.partition(itemsSet, itemsPerThread);

        List<Future<Multimap<String, String>>> computedIndex = new ArrayList<>();


        for (int i = 0; i < numThread; i++)
            computedIndex.add(threadExecutor.submit(new SingleIndexerThread(itemSubLists.get(i), propertiesManager)));

        for (Future<Multimap<String, String>> newIndexResult : computedIndex) {
            invertedIndex.putAll(newIndexResult.get());

        }

        threadExecutor.shutdown();
    }

    public Map<String, List<Double>> getScoreVector() {
        Map<String, List<Double>> scoreVectors = new HashMap<>();


        return scoreVectors;

    }

    @Override
    public String toString() {
        return this.invertedIndex.toString();
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        String propertyIndexDir = "/home/asuglia/thesis/content_lodrecsys/movielens/stored_prop",
                mappedItemFile = "mapping/item.mapping";


        PropertyIndexer indexer = new PropertyIndexer(mappedItemFile, propertyIndexDir);

        System.out.println(indexer);

    }
}
