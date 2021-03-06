package di.uniba.it.lodrecsys.utils.mapping;

import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class which uses a specific file in which there are all the mapped items,
 * in order to retrieve the selected properties for them
 */
public class PropertiesGenerator {
    public static void main(String[] args) throws Exception {
        String choosenProp = "mapping/choosen_prop.txt",
                propertiesDir = "/home/asuglia/thesis/content_lodrecsys/movielens/stored_prop",
                dbpediaMapping = "mapping/item.mapping",
                firstLevelExpProp = "mapping/exp_prop.txt";

        PropertiesManager manager = new PropertiesManager(propertiesDir);
        SPARQLClient sparql = new SPARQLClient();
        Collection<String> expPropList = loadPropertiesURI(choosenProp);
        Collection<String> missed = loadPropertiesURI("mapping/missed");


        int i = 0;

        for (String mappedItem : missed) {
            try {
                manager.start(true);
                sparql.saveResourceProperties(URLDecoder.decode(mappedItem, "UTF-8"), expPropList, manager);

                i++;

                manager.commitChanges();
            } finally {

                manager.closeManager();
            }


        }
    }


    private static void myWait(int sec) {
        //wait

        long cm = System.currentTimeMillis();
        while ((System.currentTimeMillis() - cm) < sec * 1000) ;

    }

    private static void loadProperties(PropertiesManager manager, String propFile, String dbpediaMapping) throws IOException {
        Collection<String> expPropList = loadPropertiesURI(propFile);

        SPARQLClient sparql = new SPARQLClient();

        List<MovieMapping> mappedItems = Utils.loadDBpediaMappedItems(dbpediaMapping);


        int i = 0;

        for (MovieMapping mappedItem : mappedItems) {
            try {
                manager.start(true);
                sparql.saveResourceProperties(URLDecoder.decode(mappedItem.getDbpediaURI(), "UTF-8"), expPropList, manager);

                i++;

                manager.commitChanges();
            } finally {

                manager.closeManager();
            }


        }
    }

    private static void loadFirstLevelExpansionProp(PropertiesManager manager, String firstLevelExpProp, String dbpediaMapping) throws IOException {
        Collection<String> expPropList = loadPropertiesURI(firstLevelExpProp);

        SPARQLClient sparql = new SPARQLClient();

        List<MovieMapping> mappedItems = Utils.loadDBpediaMappedItems(dbpediaMapping);


        int i = 0;

        for (MovieMapping mappedItem : mappedItems) {
            try {
                manager.start(true);
                sparql.downloadFirstLevelRelation(mappedItem.getDbpediaURI(), expPropList, manager);

                i++;

                manager.commitChanges();
            } finally {

                manager.closeManager();
            }


        }
    }


    private static Collection<String> loadPropertiesURI(String fileName) {
        BufferedReader reader = null;
        Collection<String> listProp = new ArrayList<>();

        try {
            reader = new BufferedReader(new FileReader(fileName));


            while (reader.ready()) {
                listProp.add(reader.readLine());

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return listProp;

    }

}
