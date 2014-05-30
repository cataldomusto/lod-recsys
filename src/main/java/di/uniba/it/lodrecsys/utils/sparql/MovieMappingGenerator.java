package di.uniba.it.lodrecsys.utils.sparql;

import di.uniba.it.lodrecsys.entity.MappingEntity;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.Utils;
import org.apache.lucene.search.spell.JaroWinklerDistance;

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

        System.setProperty("http.proxyHost", "wproxy.ict.uniba.it");
        System.setProperty("http.proxyPort", "80");
        System.setProperty("proxyHost", "wproxy.ict.uniba.it");
        System.setProperty("proxyPort", "80");

        SPARQLClient client = new SPARQLClient();
        List<MovieMapping> movieListFromML = Utils.getMovieTitles(itemFile);

        client.movieQuery(dbpediaItemsFile);



        /*for(MovieMapping movieML : movieListFromML) {
            if(movieListFromDB.contains(movieML)) {
                MovieMapping dbMovie = movieListFromDB.get(movieListFromDB.indexOf(movieML));
                movieML.setDbpediaURI(dbMovie.getDbpediaURI());
            }


        }

        Utils.serializeMappingList(movieListFromML, mappingFile);

        */

    }

}
