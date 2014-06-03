package di.uniba.it.lodrecsys.entity;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevensteinDistance;

/**
 * Created by asuglia on 5/30/14.
 */
public class MovieMapping extends MappingEntity {
    private String year;
    private String genre;


    public MovieMapping(String itemID, String dbpediaURI, String name, String year, String genre) {
        super(itemID, dbpediaURI, name);
        this.year = year;
        this.genre = genre;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;

    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        return super.toString() +
                " year='" + year + '\'' + " genre='" + genre + "\'" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof MovieMapping))
            return false;

        LevensteinDistance distanceMetric = new LevensteinDistance();

        MovieMapping map = (MovieMapping) o;
        String[] otherSplittedGenre = map.genre.split(" ");

        // Same dbpedia mapping entity
        if (this.dbpediaURI != null && map.getDbpediaURI() != null)
            return this.dbpediaURI.equals(map.getDbpediaURI());

        boolean findGenre = false;
        for(String genre : otherSplittedGenre)
            findGenre = this.genre.contains(genre);

        double distanceTitle = distanceMetric.getDistance(this.name, map.getName());

        return distanceTitle >= 0.85 && this.year.contains(map.getYear()) && findGenre;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (year != null ? year.hashCode() : 0);
        return result;
    }
}
