package di.uniba.it.lodrecsys.entity;

/**
 * @author pierpaolo
 */
public class RITriple implements Comparable<RITriple> {

    private String idItem;

    private String idUser;

    private float rate;

    public RITriple(String idItem, String idUser, float rate) {
        this.idItem = idItem;
        this.idUser = idUser;
        this.rate = rate;
    }

    public static RITriple create(String[] values) {
        if (values.length == 3) {
            RITriple triple = new RITriple(values[1], values[0], Float.parseFloat(values[2]));
            return triple;
        } else {
            RITriple triple = new RITriple(values[1], values[0], 0);
            return triple;
        }
    }

    public String getIdItem() {
        return idItem;
    }

    public void setIdItem(String idItem) {
        this.idItem = idItem;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "RITriple{" + "idItem=" + idItem + ", idUser=" + idUser + ", rate=" + rate + '}';
    }

    public String toFileString() {
        return idUser + "\t" + idItem + "\t" + rate;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.idItem != null ? this.idItem.hashCode() : 0);
        hash = 29 * hash + (this.idUser != null ? this.idUser.hashCode() : 0);
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
        final RITriple other = (RITriple) obj;
        if ((this.idItem == null) ? (other.idItem != null) : !this.idItem.equals(other.idItem)) {
            return false;
        }
        if ((this.idUser == null) ? (other.idUser != null) : !this.idUser.equals(other.idUser)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(RITriple o) {
        return Float.compare(o.getRate(), rate);
    }

}
