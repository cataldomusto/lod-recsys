package di.uniba.it.lodrecsys.utils.sparql;

import di.uniba.it.lodrecsys.entity.MappingEntity;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.Utils;
import org.apache.lucene.search.spell.JaroWinklerDistance;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


/**
 * Created by asuglia on 5/30/14.
 */


/**
 *
 *
 * */
public class MovieMappingGenerator {
    public static void main(String[] args) throws IOException {
        String itemFile = "mapping/u.item",
                dbpediaItemsFile = "mapping/item.dbpedia",
                dbpediaMapping = "mapping/item.mapping";

        SPARQLClient client = new SPARQLClient();
        List<MovieMapping> movieListFromML = Utils.getMovieTitles(itemFile);

        //System.out.println(movieListFromML);
        //client.movieQuery(dbpediaItemsFile);

        generateCompleteMapping(dbpediaItemsFile, movieListFromML);

        System.out.println(movieListFromML);

        Utils.serializeMappingList(movieListFromML, dbpediaMapping);



    }


    private static void generateCompleteMapping(String dbpediaItemsFile, List<MovieMapping> listML) throws IOException {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(dbpediaItemsFile));

            while(reader.ready()) {
                String[] splitted = reader.readLine().split("\t");
                if (splitted.length != 5)
                    System.out.println("splitted " + splitted[1]);

                MovieMapping dbMovie = new MovieMapping(splitted[0], splitted[2], splitted[1], splitted[3], splitted[4]);
                int filmIndex = listML.indexOf(dbMovie);
                if (filmIndex != -1) {
                    MovieMapping currMovie = listML.get(filmIndex);
                    currMovie.setDbpediaURI(dbMovie.getDbpediaURI());

                }



            }


        } catch (FileNotFoundException e) {
            throw e;
        } finally {
            if(reader != null)
                reader.close();
        }


    }

}
