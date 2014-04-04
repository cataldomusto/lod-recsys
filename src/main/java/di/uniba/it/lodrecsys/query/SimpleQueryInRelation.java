package di.uniba.it.lodrecsys.query;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import di.uniba.it.lodrecsys.query.SimpleQuery;

import java.util.List;

/**
 * @author pierpaolo
 */
public class SimpleQueryInRelation extends SimpleQuery {

    public SimpleQueryInRelation(String endpoint, String resource) {
        super(endpoint, resource);
    }

    public SimpleQueryInRelation(String endpoint, String graphURI, String resource) {
        super(endpoint, graphURI, resource);
    }

    @Override
    public List<SimpleResult> runQuery(boolean onlyIRI) {
        Query query;
        String q = " SELECT ?s ?p WHERE { ?s ?p <" + super.getResource() + "> .}";
        query = QueryFactory.create(q);
        return super.execQuery(query);
    }

}
