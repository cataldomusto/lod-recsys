package di.uniba.it.lodrecsys.entity;

/**
 * Class which represents a specialized entity for the MovieLens
 * dataset (ml-100k).
 */
public class MovieMapping extends MappingEntity implements Comparable<MovieMapping> {
    private String year;

    public MovieMapping(String itemID, String dbpediaURI, String name) {
        super(itemID, dbpediaURI, name);
        this.year = "";
    }

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
        if (!year.equals(""))
            return super.toString() +
                    " year='" + year + '\'' + '}';
        else return super.toString() + '\'' + '}';
    }


    @Override
    public int compareTo(MovieMapping movieMapping) {
        return this.dbpediaURI.compareTo(movieMapping.getDbpediaURI());
    }
}
