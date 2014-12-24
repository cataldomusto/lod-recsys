package di.uniba.it.lodrecsys.graph;

/**
 * Created by simo on 23/12/14.
 */
public class VertexPageRank implements Comparable<VertexPageRank> {
    private String property;
    private double score;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VertexPageRank that = (VertexPageRank) o;

        if (!getProperty().equals(that.getProperty())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = property.hashCode();
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public VertexPageRank(String property, double score) {
        this.property = property;
        this.score = score;
    }

    @Override
    public int compareTo(VertexPageRank o) {

        if (Double.compare(this.getScore(), o.getScore()) > 0)
            return -1;
        if (Double.compare(this.getScore(), o.getScore()) < 0)
            return 1;
        return 0;
    }
}
