/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.lodrecsys.query;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
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
public class PathQuery {

    private static final Logger logger = Logger.getLogger(PathQuery.class.getName());

    private String subject;

    private String predicate;

    private String object;

    private String endpoint;

    private String graphURI;

    public PathQuery(String endpoint, String subject, String predicate, String object) {
        this.endpoint = endpoint;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public PathQuery(String endpoint, String graphURI, String subject, String predicate, String object) {
        this.endpoint = endpoint;
        this.graphURI = graphURI;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public List<SimpleResult> runQuery(boolean onlyIRI) {
        Query query;
        String q = null;
        if (subject == null) {
            q = " SELECT ?x WHERE { ?x <" + predicate + "> <" + object + "> . ";
        }
        if (object == null) {
            q = " SELECT ?x WHERE { <" + subject + "> <" + predicate + "> ?x . ";
        }
        if (onlyIRI) {
            q += "FILTER isIRI(?x)}";
        } else {
            q += "}";
        }
        //System.out.println(q);
        query = QueryFactory.create(q);
        return execQuery(query);
    }

    private List<SimpleResult> execQuery(Query query) {

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
                prop = qs.get("x"); //get the variable
                String p = prop.toString();
                if (p.startsWith("<") && p.endsWith(">")) {
                    p = p.substring(1, p.length() - 1);
                }
                result.setPredicate(this.predicate);
                if (subject == null) {
                    result.setSubject(p);
                    result.setObject(object);
                }
                if (object == null) {
                    result.setObject(p);
                    result.setSubject(subject);
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

}
