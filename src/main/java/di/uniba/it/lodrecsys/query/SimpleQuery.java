package di.uniba.it.lodrecsys.query;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pierpaolo
 */
public abstract class SimpleQuery {

    private String endpoint;

    private String graphURI;

    private final String resource;

    private static final Logger logger = Logger.getLogger(SimpleQuery.class.getName());

    public SimpleQuery(String endpoint, String resource) {
        this.endpoint = endpoint;
        this.resource = resource;
    }

    public SimpleQuery(String endpoint, String graphURI, String resource) {
        this.endpoint = endpoint;
        this.graphURI = graphURI;
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    public List<SimpleResult> execQuery(Query query) {

        //logger.log(Level.INFO, "Executing SPARQL query: {0}", query.toString());
        List<SimpleResult> list = new ArrayList<SimpleResult>();
        QueryExecution qexec = null;
        try {
            if (graphURI == null) {
                qexec = QueryExecutionFactory.sparqlService(endpoint, query);
            } else {
                qexec = QueryExecutionFactory.sparqlService(endpoint, query, graphURI);
            }

            ResultSet results = qexec.execSelect();

            QuerySolution qs;
            RDFNode node, prop;
            while (results.hasNext()) {
                SimpleResult result = new SimpleResult();
                qs = results.next();

                if (qs.contains("p")) {
                    prop = qs.get("p"); //get the predicate of the triple
                    String p = prop.toString();
                    if (p.startsWith("<") && p.endsWith(">")) {
                        p = p.substring(1, p.length() - 1);
                    }
                    result.setPredicate(p);
                }
                if (qs.get("o") == null) {
                    node = qs.get("s"); //get the subject of the triple
                    String n = node.toString();
                    if (n.startsWith("<") && n.endsWith(">")) {
                        n = n.substring(1, n.length() - 1);
                    }
                    result.setSubject(n);
                    result.setObject(resource);
                } else {

                    node = qs.get("o"); //get the object of the triple
                    String n = node.toString();
                    if (n.startsWith("<") && n.endsWith(">")) {
                        n = n.substring(1, n.length() - 1);
                    }
                    result.setObject(n);
                    result.setSubject(resource);
                }
                list.add(result);
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error to execute query", ex);
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
        return list;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getGraphURI() {
        return graphURI;
    }

    public void setGraphURI(String graphURI) {
        this.graphURI = graphURI;
    }

    public abstract List<SimpleResult> runQuery(boolean onlyIRI);
}
