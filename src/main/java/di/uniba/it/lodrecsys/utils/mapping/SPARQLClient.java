package di.uniba.it.lodrecsys.utils.mapping;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.Utils;
import org.apache.jena.atlas.web.HttpException;

import java.io.IOException;
import java.util.*;
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

    private String formatPropertiesList(Collection<String> specificProp) {
        String formattedProperties = "";

        for (String prop : specificProp)
            formattedProperties += prop + " ";

        return formattedProperties;

    }


    public Map<String, String> getURIProperties(String uri, Collection<String> specificProp) {
        Map<String, String> propertiesURI = new HashMap<>();
        String proprVariable = "?prop",
                valueVariable = "?value",
                fixedURI = "<" + uri + ">",
                queryProp = "select " + proprVariable + " where {\n" +
                        fixedURI + " " + proprVariable + " " + valueVariable +
                        "values " + proprVariable + "{" +
                        formatPropertiesList(specificProp) + "} \n" +
                        "}";

        currLogger.info(queryProp);
        Query query = QueryFactory.create(queryProp);

        QueryExecution qexec = null;
        try {


            boolean doneIt = false;

            while (!doneIt) {

                try {
                    if (graphURI == null)
                        qexec = QueryExecutionFactory.sparqlService(endpoint, query);
                        //qexec = QueryExecutionFactory.sparqlService(newEndpoint, query);
                    else
                        qexec = QueryExecutionFactory.sparqlService(endpoint, query,
                                graphURI);

                    ResultSet resultSet = qexec.execSelect();

                    currLogger.info("Executed query!");

                    QuerySolution currSolution;

                    while (resultSet.hasNext()) {
                        currSolution = resultSet.nextSolution();

                        propertiesURI.put(currSolution.getResource(proprVariable).toString(), currSolution.getLiteral(valueVariable).toString());

                    }

                    myWait(30);
                    doneIt = true;
                } catch (Exception ex) {

                    doneIt = false;
                    currLogger.fine("Try again...");
                    propertiesURI.clear();
                    myWait(30);


                }
            }
            return propertiesURI;

        } finally {
            if (qexec != null)
                qexec.close();
        }


    }

    public Set<String> getURIProperties(String uri) {
        Set<String> propertiesURI = new TreeSet<>();
        String fixedURI = "<" + uri + ">", queryProp = "select ?prop where {\n" +
                fixedURI + " ?prop ?value\n" +
                "}", proprVariable = "?prop";

        currLogger.info(queryProp);
        Query query = QueryFactory.create(queryProp);

        QueryExecution qexec = null;
        try {


            boolean doneIt = false;

            while (!doneIt) {

                try {
                    if (graphURI == null)
                        qexec = QueryExecutionFactory.sparqlService(endpoint, query);
                        //qexec = QueryExecutionFactory.sparqlService(newEndpoint, query);
                    else
                        qexec = QueryExecutionFactory.sparqlService(endpoint, query,
                                graphURI);

                    ResultSet resultSet = qexec.execSelect();

                    currLogger.info("Executed query!");

                    QuerySolution currSolution;

                    while (resultSet.hasNext()) {
                        currSolution = resultSet.nextSolution();

                        propertiesURI.add(currSolution.getResource(proprVariable).toString());

                    }

                    myWait(30);
                    doneIt = true;
                } catch (Exception ex) {

                    doneIt = false;
                    currLogger.fine("Try again...");
                    propertiesURI.clear();
                    myWait(30);


                }
            }
            return propertiesURI;

        } finally {
            if (qexec != null)
                qexec.close();
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
        while ((System.currentTimeMillis() - cm) < sec * 1000) ;

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

            while (resultSet.hasNext()) {
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


}
