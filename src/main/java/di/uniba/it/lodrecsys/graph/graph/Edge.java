package di.uniba.it.lodrecsys.graph.graph;

/**
 * Created by simo on 22/12/14.
 */
public class Edge {
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
    public int hashCode() {
        int result = property != null ? property.hashCode() : 0;
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (!object.equals(edge.object)) return false;
        if (!property.equals(edge.property)) return false;
        if (!subject.equals(edge.subject)) return false;

        return true;
    }
}
