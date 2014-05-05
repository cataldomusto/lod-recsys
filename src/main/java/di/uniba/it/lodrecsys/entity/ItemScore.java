package di.uniba.it.lodrecsys.entity;

/**
 * @author pierpaolo
 */
public class ItemScore implements Comparable<ItemScore> {

    private String id;

    private float score;

    public ItemScore(String id, float score) {
        this.id = id;
        this.score = score;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ItemScore other = (ItemScore) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "ItemScore{" + "id=" + id + ", score=" + score + '}';
    }

    @Override
    public int compareTo(ItemScore o) {
        return Float.compare(o.getScore(), score);
    }


}
