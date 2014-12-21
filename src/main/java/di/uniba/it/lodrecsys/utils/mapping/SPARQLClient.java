package di.uniba.it.lodrecsys.utils.mapping;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import di.uniba.it.lodrecsys.entity.MovieMapping;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * A utility class used to retrieve information from the
 * DBpedia endpoint
 */
public class SPARQLClient {
    private static final String PREDICATE_WIKIPAGE = "<http://xmlns.com/foaf/0.1/isPrimaryTopicOf>";
    private static final String PREDICATE_ABSTRACT = "<http://dbpedia.org/ontology/abstract>";
    private static Logger currLogger = Logger.getLogger(SPARQLClient.class.getName());
    private String dbpediaEndpoint = "http://live.dbpedia.org/sparql";
    private String graphURI = "http://dbpedia.org";

    private String formatPropertiesList(Collection<String> specificProp, boolean areURI) {
        String formattedProperties = "";

        for (String prop : specificProp)
            formattedProperties += (areURI) ? "<" + prop + ">\n" : prop + "\n";

        return formattedProperties;

    }

    public void downloadFirstLevelRelation(String resource, Collection<String> expProperties, PropertiesManager propManager) {
        String expPropVar = "?exp_prop", expPropValueVar = "?exp_prop_value",
                itemVar = "?item", formattedResource = "<" + resource + ">",
                currQuery =
                        "SELECT DISTINCT *\n" +
                                "WHERE\n" +
                                "  { " + formattedResource + " " + expPropVar + " " + expPropValueVar + " .\n" +
                                itemVar + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Film> .\n" +
                                itemVar + " " + expPropVar + " " + expPropValueVar + ".\n" +
                                "VALUES " + expPropVar + " { " + formatPropertiesList(expProperties, true) + " }\n" +
                                "  }";

        currLogger.info(currQuery);
        Query query = QueryFactory.create(currQuery);

        QueryExecution qexec = null;


        try {
            qexec = QueryExecutionFactory.sparqlService(dbpediaEndpoint, query);

            ResultSet resultSet = qexec.execSelect();

            currLogger.info("Executed query!");

            QuerySolution currSolution;

            while (resultSet.hasNext()) {
                currSolution = resultSet.nextSolution();
                propManager.addSolution(currSolution);
            }

        } finally {

            if (qexec != null)
                qexec.close();
        }


    }

    public String getWikipediaURI(String resourceURI) {

        String uriVar = "?uri", wikiURIQuery = "select * where {" +
                "<" + resourceURI + "> " + PREDICATE_WIKIPAGE + " " + uriVar + "}";

        currLogger.info(wikiURIQuery);
        Query query = QueryFactory.create(wikiURIQuery);

        QueryExecution qexec = null;

        String wikiURI = null;

        try {
            qexec = QueryExecutionFactory.sparqlService(dbpediaEndpoint, query);

            ResultSet resultSet = qexec.execSelect();

            currLogger.info("Executed query!");

            QuerySolution currSolution;

            while (resultSet.hasNext()) {
                currSolution = resultSet.nextSolution();
                wikiURI = currSolution.getResource(uriVar).toString();
            }

        } finally {

            if (qexec != null)
                qexec.close();
        }


        return wikiURI;
    }

    public String getResourceAbstract(String resourceURI) {

        String uriVar = "?uri", abstractQuery = "select * where {" +
                "<" + resourceURI + "> " + PREDICATE_ABSTRACT + " " + uriVar + "}";

        currLogger.info(abstractQuery);
        Query query = QueryFactory.create(abstractQuery);

        QueryExecution qexec = null;

        String wikiURI = null;

        try {
            qexec = QueryExecutionFactory.sparqlService(dbpediaEndpoint, query);

            ResultSet resultSet = qexec.execSelect();

            currLogger.info("Executed query!");

            QuerySolution currSolution;

            while (resultSet.hasNext()) {
                currSolution = resultSet.nextSolution();
                wikiURI = currSolution.get(uriVar).toString();
            }

        } finally {

            if (qexec != null)
                qexec.close();
        }


        return wikiURI;

    }

    public Map<String, String> getURIfromWikiID(Set<String> wikiIDList) {
        String dbpediaVar = "?dbpedia_uri",
                queryWiki = "SELECT DISTINCT " + dbpediaVar + "\n" +
                        "WHERE\n" +
                        "  { " + dbpediaVar + " <http://dbpedia.org/ontology/wikiPageID> %s\n" +

                        "  }";

        Map<String, String> wikiIDURI = new HashMap<>();

        for (String wikiID : wikiIDList) {

            String realQuery = String.format(queryWiki, wikiID);

            currLogger.info(realQuery);

            Query currQuery = QueryFactory.create(realQuery);

            QueryExecution execution = null;
            try {
                execution = QueryExecutionFactory.sparqlService(dbpediaEndpoint, currQuery);

                ResultSet resultSet = execution.execSelect();
                QuerySolution solution = null;

                while (resultSet.hasNext()) {
                    solution = resultSet.nextSolution();

                    wikiIDURI.put(wikiID, solution.get(dbpediaVar).toString());
                }

            } finally {
                if (execution != null)
                    execution.close();
            }
        }


        return wikiIDURI;
    }

    public void saveResourceProperties(String resourceURI, Collection<String> specificProp, PropertiesManager propManager) {

        String proprVariable = "?prop",
                valueVariable = "?value",
                fixedURI = "<" + resourceURI + ">",
                queryProp = "select " + proprVariable + " " + valueVariable + " where {\n" +
                        fixedURI + " " + proprVariable + " " + valueVariable +
                        " values " + proprVariable + "{" + formatPropertiesList(specificProp, true) + "} \n" +
                        "}";
        currLogger.info(queryProp);
        Query query = QueryFactory.create(queryProp);

        QueryExecution qexec = null;
        try {
            qexec = QueryExecutionFactory.sparqlService(dbpediaEndpoint, query);

            ResultSet resultSet = qexec.execSelect();

            currLogger.info("Executed query!");

            QuerySolution currSolution;


            while (resultSet.hasNext()) {
                currSolution = resultSet.nextSolution();
                propManager.addSolution(currSolution, resourceURI);


            }


        } finally {

            if (qexec != null)
                qexec.close();

            // writer.close();

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
                        qexec = QueryExecutionFactory.sparqlService(dbpediaEndpoint, query);
                        //qexec = QueryExecutionFactory.sparqlService(newEndpoint, query);
                    else
                        qexec = QueryExecutionFactory.sparqlService(dbpediaEndpoint, query,
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
                qexec = QueryExecutionFactory.sparqlService(dbpediaEndpoint, query);
                //qexec = QueryExecutionFactory.sparqlService(newEndpoint, query);
            else
                qexec = QueryExecutionFactory.sparqlService(dbpediaEndpoint, query,
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
