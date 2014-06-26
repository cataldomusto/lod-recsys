package di.uniba.it.lodrecsys.utils.mapping;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Created by asuglia on 5/30/14.
 */
public class MovieMappingGenerator {
    private static Logger currLogger = Logger.getLogger(MovieMappingGenerator.class.getName());
    public static void main(String[] args) throws IOException {
        String itemFile = "mapping/u.item",
                dbpediaItemsFile = "mapping/item.dbpedia",
                dbpediaMapping = "mapping/item.mapping";

        SPARQLClient client = new SPARQLClient();
        List<MovieMapping> movieListFromML = Utils.getMovieList(itemFile),
                dbpediaList = Utils.loadDBpediaMappingItems(dbpediaItemsFile);

        List<MovieMapping> mapped = generateCompleteMapping(dbpediaList, movieListFromML);


    }


    private static List<MovieMapping> generateCompleteMapping(List<MovieMapping> dbpediaItems, List<MovieMapping> listML) throws IOException {

        List<MovieMapping> mapped = new ArrayList<>();

        for (MovieMapping film : listML) {
            int filmIndex = dbpediaItems.indexOf(film);
            if (filmIndex != -1) {
                MovieMapping currMovie = dbpediaItems.get(filmIndex);
                film.setDbpediaURI(currMovie.getDbpediaURI());
                mapped.add(film);
            } else {
                mapped.add(film);
                currLogger.info("Mapped: " + film);
            }


        }

        return mapped;
    }


    private static List<MovieMapping> loadUnmappedItems(String dbpediaMapping) throws IOException {
        BufferedReader reader = null;
        List<MovieMapping> unmapped = new ArrayList<>();


        try {

            reader = new BufferedReader(new FileReader(dbpediaMapping));

            while (reader.ready()) {
                String[] currLineSplitted = reader.readLine().split("\t");
                if (currLineSplitted[2].equals("null"))
                    unmapped.add(new MovieMapping(currLineSplitted[0], currLineSplitted[2], currLineSplitted[1], currLineSplitted[3]));

            }

            return unmapped;

        } catch (IOException e) {
            throw e;
        } finally {
            if (reader != null)
                reader.close();
        }



    }

}
