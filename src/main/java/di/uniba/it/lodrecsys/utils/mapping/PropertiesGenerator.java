package di.uniba.it.lodrecsys.utils.mapping;

import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by asuglia on 6/26/14.
 */
public class PropertiesGenerator {
    public static void main(String[] args) throws Exception {
        String choosenProp = "mapping/choosen_prop.txt",
                propertiesDir = "/home/asuglia/thesis/content_lodrecsys/movielens/stored_prop_exp",
                dbpediaMapping = "mapping/item.mapping",
                firstLevelExpProp = "mapping/exp_prop.txt";

        PropertiesManager manager = new PropertiesManager(propertiesDir);

        List<Statement> statements = manager.getResourceProperties("http://dbpedia.org/resource/Quentin_Tarantino");

        if (!statements.isEmpty())
            System.out.println("Correct tarantino");

        statements = manager.getResourceProperties("http://dbpedia.org/resource/Pulp_Fiction");

        if (!statements.isEmpty())
            System.out.println("Correct pulp");


    }


    private static void myWait(int sec) {
        //wait

        long cm = System.currentTimeMillis();
        while ((System.currentTimeMillis() - cm) < sec * 1000) ;

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
