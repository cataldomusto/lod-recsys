package di.uniba.it.lodrecsys.entity;

import org.apache.lucene.search.spell.JaroWinklerDistance;

/**
 * Created by asuglia on 5/30/14.
 */
public class MovieMapping extends MappingEntity {
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
                " year='" + year + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        JaroWinklerDistance distanceMetric = new JaroWinklerDistance();
        if(!(o instanceof MovieMapping))
            return false;
        MovieMapping map = (MovieMapping) o;
        double distance = distanceMetric.getDistance(this.name, map.getName());

        return distance >= 0.75 && this.year.contains(map.getYear());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (year != null ? year.hashCode() : 0);
        return result;
    }
}
