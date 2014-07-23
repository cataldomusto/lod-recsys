package di.uniba.it.lodrecsys.graph.indexer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
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
    private Map<String, Map<String, String>> itemRepresentation;

    public PropertyIndexer(String dbpediaMappingFile, String propertyIndexDir) throws IOException, ExecutionException, InterruptedException {
        itemRepresentation = new HashMap<>();
        invertedIndex = HashMultimap.create();
        indexItems(Utils.loadDBpediaMappedItems(dbpediaMappingFile), new PropertiesManager(propertyIndexDir), 5);
    }

    private void indexItems(List<MovieMapping> itemsSet, PropertiesManager propertiesManager, int numThread) throws ExecutionException, InterruptedException {
        int itemsPerThread = itemsSet.size() / numThread;

        ExecutorService threadExecutor = Executors.newCachedThreadPool();

        List<List<MovieMapping>> itemSubLists = com.google.common.collect.Lists.partition(itemsSet, itemsPerThread);

        List<Future<Pair<Multimap<String, String>, Map<String, Map<String, String>>>>> computedIndex = new ArrayList<>();


        for (int i = 0; i < numThread; i++)
            computedIndex.add(threadExecutor.submit(new SingleIndexerThread(itemSubLists.get(i), propertiesManager)));

        for (Future<Pair<Multimap<String, String>, Map<String, Map<String, String>>>> newIndexResult : computedIndex) {
            Pair<Multimap<String, String>, Map<String, Map<String, String>>> pair = newIndexResult.get();
            invertedIndex.putAll(pair.key);
            itemRepresentation.putAll(pair.value);

        }

        threadExecutor.shutdown();
    }

    public Map<String, Map<String, Double>> getScoreVector(List<String> userTrainingSet) {
        Map<String, Map<String, Double>> scoreVectors = new HashMap<>();

        for (String itemID : userTrainingSet) {
        }



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
