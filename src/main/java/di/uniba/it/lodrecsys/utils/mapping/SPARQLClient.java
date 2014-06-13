package di.uniba.it.lodrecsys.utils.mapping;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.Utils;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Created by asuglia on 5/27/14.
 */
public class SPARQLClient {
    private String resource;
    String property;
    private String endpoint = "http://dbpedia.org/sparql";
    private String newEndpoint = "http://live.dbpedia.org/sparql";
    private String graphURI = "http://dbpedia.org";
    private static Logger currLogger = Logger.getLogger(SPARQLClient.class.getName());

    public void exec(String resource, String prop) {
        this.resource = resource;
        this.property = prop;
        Query query;
        String q;

        String resourceQuery = "<" + resource + ">";
        String propQuery = "<" + prop + ">";
        // creation of a mapping query for getting all the resources connected to resource
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
        // creation of a mapping query for getting all the resources connected to resource
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


    public void movieQuery(String dbpediaFilms) throws IOException {
        String includeNamespaces = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>\n" +
                "PREFIX dbpprop: <http://dbpedia.org/property/>";

        String templateQuery = includeNamespaces + "\n" +
                "SELECT DISTINCT  ?movie (str(?title) AS ?movie_title) " +
                "(str(?date_onto) AS ?movie_year) (str(sample(?rel_date)) AS ?date_sub)\n" +
                "WHERE\n" +
                "  { ?movie rdf:type dbpedia-owl:Film .\n" +
                "    ?movie rdfs:label ?title\n" +
                "    FILTER langMatches(lang(?title), \"EN\")\n" +
                "    OPTIONAL\n" +
                "      { ?movie dbpedia-owl:releaseDate ?date_onto }\n" +
                "    OPTIONAL\n" +
                "      { ?movie dcterms:subject ?subject .\n" +
                "        ?subject rdfs:label ?rel_date\n" +
                "        FILTER regex(?rel_date, \".*[0-9]{4}.*\", \"i\")\n" +
                "      }\n" +
                "  }\n" +
                "GROUP BY ?movie ?title ?date_onto ?rel_date limit 2000 offset %s";
        
        int totalNumberOfFilms = 77794;
        int totNumQuery = 39;
        int offset = 0;
        int currNum = 0;

        Set<MovieMapping> mappings = new TreeSet<>();
        boolean isFull = true;

        while (isFull) {
            try {

                Query query = QueryFactory.create(String.format(templateQuery, "" + offset));
                Set<MovieMapping> currResultSet = getMovieMappingList(query);

                isFull = !currResultSet.isEmpty();
                mappings.addAll(currResultSet);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }

            offset += 2000;

            myWait(30);

        }

        //Utils.serializeMappingList(mappings, dbpediaFilms);


        System.out.println(currNum);


    }

    private void myWait(int sec) {
        //wait

        long cm = System.currentTimeMillis();
        while ((System.currentTimeMillis() - cm) < sec * 1000);

    }

    public Set<String> getURIProperties(String uri) {
        Set<String> propertiesURI = new TreeSet<>();
        String fixedURI = "<" + uri + ">", queryProp = "select distinct ?prop where {\n" +
                fixedURI + " ?prop ?value\n" +
                "}", proprVariable = "?prop";

        currLogger.info(queryProp);
        Query query = QueryFactory.create(queryProp);

        QueryExecution qexec = null;
        try {
            if (graphURI == null)
                qexec = QueryExecutionFactory.sparqlService(endpoint, query);
                //qexec = QueryExecutionFactory.sparqlService(newEndpoint, query);
            else
                qexec = QueryExecutionFactory.sparqlService(endpoint, query,
                        graphURI);

            ResultSet resultSet = qexec.execSelect();
            Set<MovieMapping> moviesList = new TreeSet<>();

            QuerySolution currSolution;

            while(resultSet.hasNext()) {
                currSolution = resultSet.nextSolution();

                propertiesURI.add(currSolution.getResource(proprVariable).toString());

            }

            myWait(30);

            return propertiesURI;

        } finally {
            if (qexec != null)
                qexec.close();
        }

    }


    private Set<MovieMapping> getMovieMappingList(Query query) {
        String dbpediaResVar = "?movie", movieTitleVar = "?movie_title",
                movieDateOnto = "?movie_year",
                movieDateTerms = "?date_sub";

        System.out.println("executing query : " + query.toString());

        QueryExecution qexec = null;
        try {
            if (graphURI == null)
                qexec = QueryExecutionFactory.sparqlService(endpoint, query);
                //qexec = QueryExecutionFactory.sparqlService(newEndpoint, query);
            else
                qexec = QueryExecutionFactory.sparqlService(endpoint, query,
                        graphURI);

            ResultSet resultSet = qexec.execSelect();
            Set<MovieMapping> moviesList = new TreeSet<>();

            QuerySolution currSolution;

            while(resultSet.hasNext()) {
                currSolution = resultSet.nextSolution();
                Literal yearOnto = currSolution.getLiteral(movieDateOnto), yearSub = currSolution.getLiteral(movieDateTerms);
                String year;

                if (yearOnto == null && yearSub == null)
                    continue; // skip the current fact

                if (yearOnto != null)
                    year = yearOnto.toString();
                else
                    year = yearSub.toString();

//                MovieMapping map = new MovieMapping(null, currSolution.getResource(dbpediaResVar).getURI(),
//                        currSolution.getLiteral(movieTitleVar).toString(),
//                        year);

                //moviesList.add(map);

            }

            return moviesList;

        } finally {
            if (qexec != null)
                qexec.close();
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
