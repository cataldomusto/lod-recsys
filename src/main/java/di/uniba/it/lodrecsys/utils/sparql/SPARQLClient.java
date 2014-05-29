package di.uniba.it.lodrecsys.utils.sparql;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Created by asuglia on 5/27/14.
 */
public class SPARQLClient {
    private String resource;
    String property;
    private String endpoint = "http://dbpedia.org/sparql";
    private String graphURI = "http://dbpedia.org";

    public void exec(String resource, String prop) {
        this.resource = resource;
        this.property = prop;
        Query query;
        String q;

        String resourceQuery = "<" + resource + ">";
        String propQuery = "<" + prop + ">";
        // creation of a sparql query for getting all the resources connected to resource
        //the FILTER isIRI is used to get only resources, so this query descards any literal or data-type

        q = " SELECT * WHERE {{" + " ?s " + propQuery + " " + resourceQuery
                + ". " + "FILTER isIRI(?s). " + " } UNION {" + resourceQuery + " "
                + propQuery + " ?o " + "FILTER isIRI(?o). " + "}}";
        try {
            query = QueryFactory.create(q);

            execQuery(query);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void exec(String resource) {
        this.resource = resource;
        Query query;
        String q;

        String resourceQuery = "<" + resource + ">";
        // creation of a sparql query for getting all the resources connected to resource
        //the FILTER isIRI is used to get only resources, so this query descards any literal or data-type

        q = " SELECT * WHERE {{" + " ?s ?p " + resourceQuery
                + ". " + "FILTER isIRI(?s). " + " } UNION {" + resourceQuery +
                " ?p ?o " + "FILTER isIRI(?o). " + "}}";
        try {
            query = QueryFactory.create(q);

            execQuery(query);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void execQuery(Query query) {

        System.out.println("executing query : " + query.toString());

        QueryExecution qexec = null;
        try {
            if (graphURI == null)
                qexec = QueryExecutionFactory.sparqlService(endpoint, query);
            else
                qexec = QueryExecutionFactory.sparqlService(endpoint, query,
                        graphURI);

            ResultSet results = qexec.execSelect();

            QuerySolution qs;
            RDFNode node, prop;

            String n = "", p = this.property;

            System.out.println("Results:");
            //iteration over the resultset
            while (results.hasNext()) {

                qs = results.next();

                if (qs.contains("p")) {
                    prop = qs.get("p"); //get the predicate of the triple
                    p = prop.toString();
                    p = p.replace("<", "");
                    p = p.replace(">", "");

                }
                if (qs.get("o") == null) {
                    node = qs.get("s"); //get the subject of the triple
                    n = node.toString();
                    n = n.replace("<", "");
                    n = n.replace(">", "");

                    System.out.println(n + '\t' + p + '\t' + resource);
                } else {

                    node = qs.get("o"); //get the object of the triple
                    n = node.toString();
                    n = n.replace("<", "");
                    n = n.replace(">", "");

                    System.out.println(resource + '\t' + p + '\t' + n);

                }

            }

        } finally {
            if (qexec != null)
                qexec.close();
        }

    }

    public static void main(String[] args) {

        SPARQLClient exec = new SPARQLClient();
        //get all the triples related to the predicate http://dbpedia.org/ontology/starring
        //wherein the Godfather appears as subject or object
        exec.exec("http://dbpedia.org/resource/The_Godfather", "http://dbpedia.org/ontology/starring");

        //get all the triples that involve the Godfather
        //exec.exec("http://dbpedia.org/resource/The_Godfather");


    }
}
