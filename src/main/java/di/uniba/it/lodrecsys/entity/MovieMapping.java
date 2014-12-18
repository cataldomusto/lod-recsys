package di.uniba.it.lodrecsys.entity;

import di.uniba.it.lodrecsys.utils.Utils;
import org.apache.lucene.search.spell.LevensteinDistance;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which represents a specialized entity for the MovieLens
 * dataset (ml-100k).
 */
public class MovieMapping extends MappingEntity implements Comparable<MovieMapping> {
    private String year;


    public MovieMapping(String itemID, String dbpediaURI, String name, String year) {
        super(itemID, dbpediaURI, name);
        this.year = year;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;

    }


    @Override
    public String toString() {
        return super.toString() +
                " year='" + year + '\'' + '}';
    }

    /**
     * Defines when two distinct object are equal according to
     * an heuristic specific for the ml-100k dataset
     *
     * @param o an other movie
     * @return <code>true</code> if the two movie are equal, <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MovieMapping))
            return false;

        MovieMapping map = (MovieMapping) o;


        if (this.dbpediaURI != null && map.dbpediaURI != null && this.dbpediaURI.equals(map.dbpediaURI))
            return true; // same dbpedia uri - same resource

        LevensteinDistance distanceMetric = new LevensteinDistance();
        Pattern numberOnly = Pattern.compile("[0-9]{4}");
        Pattern titlePattern = Pattern.compile("(.*)\\(.*\\)");
        Pattern numbers = Pattern.compile(".*([0-9]{4}).*");

        Matcher matchTitle = titlePattern.matcher(this.getName());
        String realThisTitle = this.getName(), realMapTitle = map.getName();

        if (matchTitle.find()) {
            realThisTitle = matchTitle.group(1);

        }

        matchTitle = titlePattern.matcher(map.getName());

        if (matchTitle.find()) {
            realMapTitle = matchTitle.group(1);
        }

        realThisTitle = realThisTitle.trim();
        realMapTitle = realMapTitle.trim();

        Set<String> first = Utils.tokenizeString(realThisTitle), second = Utils.tokenizeString(realMapTitle);
        Set<String> union = new TreeSet<>(), inter = new TreeSet<>();

        union.addAll(first);

        union.addAll(second);
        for (String elem : second)
            if (first.contains(elem))
                inter.add(elem);
        double jaccard = ((double) inter.size() / (double) union.size());

        double distanceTitle = distanceMetric.getDistance(realThisTitle, realMapTitle);


        // Mask, The and The Mask
        // Lion King, The and The Lion King

        if (jaccard > 0.9) {

            if (numberOnly.matcher(map.getYear()).matches()) {
                double distanceYear =
                        distanceMetric.getDistance(this.year, map.getYear());

                return distanceYear > 0.9;
            } else {
                Matcher matcher = numbers.matcher(map.getYear());
                if (matcher.find()) {
                    String currYear = matcher.group(1);
                    double distanceYear = distanceMetric.getDistance(currYear, this.year);

                    if (distanceTitle > 0.8)
                        return distanceYear > 0.5;

                    return distanceYear > 0.7;
                }

            }
        }

        return false;
    }


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
