package di.uniba.it.lodrecsys.entity;


import java.io.Serializable;

/**
 * Class that stores the score associated to each item
 * by the recommender. When inserted in a ordered data structure
 * will be automatically ordered decreasing order due to
 * the specific *compareTo* implementation.
 */
public class Rating implements Comparable<Rating>, Serializable {
    private String itemID;
    private String rating;

    public Rating(String itemID, String rating) {
        this.itemID = itemID;
        this.rating = rating;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating)) return false;

        Rating rating1 = (Rating) o;

        if (itemID != null ? !itemID.equals(rating1.itemID) : rating1.itemID != null) return false;
        if (rating != null ? !rating.equals(rating1.rating) : rating1.rating != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = itemID != null ? itemID.hashCode() : 0;
        result = 31 * result + (rating != null ? rating.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Rating rating) {
        assert rating != null;
        Double firstRate = Double.parseDouble(this.rating),
                secRate = Double.parseDouble(rating.getRating());
        int sortVal = -1 * firstRate.compareTo(secRate);
        return (sortVal == 0) ? this.itemID.compareTo(rating.getItemID()) : sortVal; // reverts the result of the compareTo
    }

    @Override
    public String toString() {
        return "Rating{" +
                "itemID='" + itemID + '\'' +
                ", rating='" + rating + '\'' +
                '}';
    }
}

