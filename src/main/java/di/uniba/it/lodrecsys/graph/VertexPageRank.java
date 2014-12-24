package di.uniba.it.lodrecsys.graph;

/**
 * Created by simo on 23/12/14.
 */
public class VertexPageRank implements Comparable<VertexPageRank> {
    private String resourceURI;
    private double score;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VertexPageRank that = (VertexPageRank) o;

        if (Double.compare(that.score, score) != 0) return false;
        if (!resourceURI.equals(that.resourceURI)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = resourceURI.hashCode();
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public VertexPageRank(String resourceURI, double score) {
        this.resourceURI = resourceURI;
        this.score = score;
    }

    @Override
    public int compareTo(VertexPageRank o) {
        if (this.getScore() > o.getScore())
            return -1;
        if (this.getScore() < o.getScore())
            return 1;
        return 0;
    }
}
