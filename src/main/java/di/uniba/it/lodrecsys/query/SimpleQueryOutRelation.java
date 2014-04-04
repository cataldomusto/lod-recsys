package di.uniba.it.lodrecsys.query;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import java.util.List;

/**
 * @author pierpaolo
 */
public class SimpleQueryOutRelation extends SimpleQuery {

    public SimpleQueryOutRelation(String endpoint, String resource) {
        super(endpoint, resource);
    }

    public SimpleQueryOutRelation(String endpoint, String graphURI, String resource) {
        super(endpoint, graphURI, resource);
    }

    @Override
    public List<SimpleResult> runQuery(boolean onlyIRI) {
        Query query;
        String q;
        if (onlyIRI) {
            q = " SELECT ?p ?o WHERE { <" + super.getResource() + "> ?p ?o . FILTER isIRI(?o)}";
        } else {
            q = " SELECT ?p ?o WHERE { <" + super.getResource() + "> ?p ?o .}";
        }
        query = QueryFactory.create(q);
        return super.execQuery(query);
    }

}
