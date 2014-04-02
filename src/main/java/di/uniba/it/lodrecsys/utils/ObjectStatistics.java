package di.uniba.it.lodrecsys.utils;

/**
 * @author pierpaolo
 */
public class ObjectStatistics implements Comparable<ObjectStatistics> {

    private String id;

    private float pos;

    private float neg;

    private float ac;

    public ObjectStatistics(String id) {
        this.id = id;
    }

    public ObjectStatistics(String id, float pos, float neg) {
        this.id = id;
        this.pos = pos;
        this.neg = neg;
    }

    public ObjectStatistics(String id, float pos, float neg, float ac) {
        this.id = id;
        this.pos = pos;
        this.neg = neg;
        this.ac = ac;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getPos() {
        return pos;
    }

    public void setPos(float pos) {
        this.pos = pos;
    }

    public float getNeg() {
        return neg;
    }

    public void setNeg(float neg) {
        this.neg = neg;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
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
        final ObjectStatistics other = (ObjectStatistics) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ObjectStatistics o) {
        return Float.compare(o.pos, pos);
    }

    public float getAc() {
        return ac;
    }

    public void setAc(float ac) {
        this.ac = ac;
    }

}
