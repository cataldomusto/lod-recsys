package di.uniba.it.lodrecsys.utils.mapping;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class which uses a specific file in which there are all the mapped items,
 * in order to retrieve the selected properties for them
 */
public class PropertiesGenerator {

    private static void extractAllFeatures() throws IOException {
        String pathWriter = LoadProperties.MAPPINGPATH + "/all_prop";
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));


        SPARQLClient sparql = new SPARQLClient();
        List<String> lines = Files.readAllLines(Paths.get(LoadProperties.MAPPINGPATH + "/books"),
                Charset.defaultCharset());
        for (String line : lines) {
            Collection<String> aa = sparql.getURIProperties(line);
            for (String s : aa) {
                out.append(s).append("\n");
            }
        }
        out.close();

    }

    public static void main(String[] args) throws Exception {

//                dbpediaMapping = "mapping/item.mapping",
//                firstLevelExpProp = "mapping/exp_prop.txt";

        SPARQLClient sparql = new SPARQLClient();
        Collection<String> expPropList = loadPropertiesURI(LoadProperties.CHOOSENPROP);
        Collection<String> missed = loadPropertiesURI(LoadProperties.MISSEDPROP);
        PropertiesManager manager = new PropertiesManager(LoadProperties.PROPERTYINDEXDIR);

        int i = 1;

        for (String mappedItem : missed) {
            try {
                manager.start(true);
                sparql.saveResourceProperties(URLDecoder.decode(mappedItem, "UTF-8"), expPropList, manager);
                System.out.println("Book " + i + " to " + missed.size() + " finished");
                i++;
                manager.commitChanges();
            } finally {

                manager.closeManager();
            }
        }
        System.out.println("Finished");
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
