package di.uniba.it.lodrecsys.query;

/**
 * @author pierpaolo
 */
public class SimpleResult {

    private String subject;

    private String predicate;

    private String object;

    public SimpleResult(String subject, String relation, String object) {
        this.subject = subject;
        this.predicate = relation;
        this.object = object;
    }

    public SimpleResult() {
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.subject != null ? this.subject.hashCode() : 0);
        hash = 37 * hash + (this.predicate != null ? this.predicate.hashCode() : 0);
        hash = 37 * hash + (this.object != null ? this.object.hashCode() : 0);
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
        final SimpleResult other = (SimpleResult) obj;
        if ((this.subject == null) ? (other.subject != null) : !this.subject.equals(other.subject)) {
            return false;
        }
        if ((this.predicate == null) ? (other.predicate != null) : !this.predicate.equals(other.predicate)) {
            return false;
        }
        if ((this.object == null) ? (other.object != null) : !this.object.equals(other.object)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SimpleResult{" + "subject=" + subject + ", relation=" + predicate + ", object=" + object + '}';
    }


}
