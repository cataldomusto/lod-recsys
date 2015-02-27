package di.uniba.it.lodrecsys.entity;

/**
 * Class which represents a specialized entity for the Books
 * dataset (books-8k).
 */
public class BookMapping extends MappingEntity implements Comparable<BookMapping> {


    public BookMapping(String itemID, String dbpediaURI, String name) {
        super(itemID, dbpediaURI, name);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Defines when two distinct object are equal according to
     * an heuristic specific for the books-8k dataset
     *
     * @param o an other movie
     * @return <code>true</code> if the two book are equal, <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BookMapping))
            return false;

        BookMapping map = (BookMapping) o;

        return this.dbpediaURI != null && map.dbpediaURI != null && this.dbpediaURI.equals(map.dbpediaURI);

    }

    @Override
    public int compareTo(BookMapping bookMapping) {
        return this.dbpediaURI.compareTo(bookMapping.getDbpediaURI());
    }
}
