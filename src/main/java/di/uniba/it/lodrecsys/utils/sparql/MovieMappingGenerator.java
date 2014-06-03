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
        //List<MovieMapping> movieListFromML = Utils.getMovieTitles(itemFile);

        //System.out.println(movieListFromML);
        client.movieQuery(dbpediaItemsFile);

        //generateCompleteMapping(dbpediaItemsFile, movieListFromML);

        //System.out.println(movieListFromML);

        /*for(MovieMapping movieML : movieListFromML) {
            if(movieListFromDB.contains(movieML)) {
                MovieMapping dbMovie = movieListFromDB.get(movieListFromDB.indexOf(movieML));
                movieML.setDbpediaURI(dbMovie.getDbpediaURI());
            }


        }

        Utils.serializeMappingList(movieListFromML, mappingFile);

        */

    }


    private static void generateCompleteMapping(String dbpediaItemsFile, List<MovieMapping> listML) throws IOException {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(dbpediaItemsFile));

            while(reader.ready()) {
                String[] splitted = reader.readLine().split("\t");

                MovieMapping dbMovie = new MovieMapping(splitted[0], splitted[1], splitted[2], splitted[3], splitted[4]);
                if(listML.contains(dbMovie)) {
                    MovieMapping currMovie = listML.get(listML.indexOf(dbMovie));
                    currMovie.setItemID(dbMovie.getDbpediaURI());

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
