package di.uniba.it.lodrecsys.entity;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevensteinDistance;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by asuglia on 5/30/14.
 */
public class MovieMapping extends MappingEntity implements Comparable<MovieMapping> {
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
        if (!(o instanceof MovieMapping))
            return false;

        MovieMapping map = (MovieMapping) o;


        if (this.dbpediaURI != null && map.dbpediaURI != null && this.dbpediaURI.equals(map.dbpediaURI))
            return true; // same dbpedia uri - same resource

        LevensteinDistance distanceMetric = new LevensteinDistance();

        Pattern titlePattern = Pattern.compile("(.*)\\(.*(film)??.*\\)");

        Matcher matchTitle = titlePattern.matcher(this.getName());
        String realThisTitle = this.getName(), realMapTitle = map.getName();

        if (matchTitle.find()) {
            realThisTitle = matchTitle.group(1);

        }

        matchTitle = titlePattern.matcher(map.getName());

        if (matchTitle.find()) {
            realMapTitle = matchTitle.group(1);
        }

        double distanceTitle = distanceMetric.getDistance(realThisTitle, realMapTitle);

        // The two films are equals
        if (distanceTitle > 0.80) {
            return true;
        }

        // Not perfect match in the title
        // Check the other properties
        if (distanceTitle > 0.65 && distanceTitle <= 0.80) {
            String[] thisSplitGenre = this.genre.split(" "),
                    otherSplitGenre = map.genre.split(" ");

            Set<String> first = new TreeSet<>(),
                    second = new TreeSet<>(), intersect = new TreeSet<>();

            first.addAll(Arrays.asList(thisSplitGenre));
            second.addAll(Arrays.asList(otherSplitGenre));

            intersect.addAll(first);
            intersect.retainAll(second);

            double jaccardIndexGenre = intersect.size() / (first.size() + second.size());

            Pattern numberOnly = Pattern.compile("[0-9]{4}");
            if (numberOnly.matcher(this.year).find()) {
                return this.year.contains(map.getYear());
            } else {
                Pattern numbers = Pattern.compile(".*([0-9]{4}).*");
                Matcher match = numbers.matcher(this.year);

                // got a match
                if (match.find()) {
                    String realYear = match.group(1);

                    return distanceMetric.getDistance(realYear, map.getYear()) > 0.75;

                }
            }

        }

        return false;

    }


//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof MovieMapping)) return false;
//        if (!super.equals(o)) return false;
//
//        MovieMapping that = (MovieMapping) o;
//
//        if(that.getDbpediaURI() != null && this.getDbpediaURI() != null)
//            return that.getDbpediaURI().equals(this.getDbpediaURI());
//
//        return this.getName().equals(that.getName());
//
//
//    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (year != null ? year.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(MovieMapping movieMapping) {
        return this.dbpediaURI.compareTo(movieMapping.getDbpediaURI());
    }
}
