package di.uniba.it.lodrecsys.graph;

import java.io.Serializable;

/**
 * Created by simo on 22/12/14.
 */
public class Edge implements Serializable {
    private String property;
    private String subject;
    private String object;

    public Edge(String property, String subject, String object) {
        this.property = property;
        this.subject = subject;
        this.object = object;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (object != null ? !object.equals(edge.object) : edge.object != null) return false;
        if (property != null ? !property.equals(edge.property) : edge.property != null) return false;
        if (subject != null ? !subject.equals(edge.subject) : edge.subject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = property != null ? property.hashCode() : 0;
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }
}
