package di.uniba.it.lodrecsys.utils;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import di.uniba.it.lodrecsys.query.SimpleQueryInRelation;
import di.uniba.it.lodrecsys.query.SimpleQueryOutRelation;
import di.uniba.it.lodrecsys.query.SimpleResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author pierpaolo
 */
public class DownloadEntityData {

    private static final Logger logger = Logger.getLogger(DownloadEntityData.class.getName());

    private static final String PREDICATE_WIKIPAGE = "http://xmlns.com/foaf/0.1/isPrimaryTopicOf";

    private static final String PREDICATE_ABSTRACT = "http://dbpedia.org/ontology/abstract";

    private static final int MAX_ATTEMPT = 3;

    private Map<String, Integer> extractToken(String text, Analyzer analyzer) throws IOException {
        TokenStream ts = analyzer.tokenStream(null, new StringReader(text));
        Map<String, Integer> map = new HashMap<String, Integer>();
        if (ts == null) {
            return map;
        }
        CharTermAttribute cattr = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            String token = cattr.toString();
            Integer w = map.get(token);
            if (w == null) {
                map.put(token, 1);
            } else {
                map.put(token, w + 1);
            }
        }
        ts.end();
        ts.close();
        return map;
    }

    private void writeMapToFile(Map<String, Integer> map, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            writer.append(entry.getKey()).append("\t").append(entry.getValue().toString());
            writer.newLine();
        }
        writer.close();
    }

    /*private void download(String startFile, String outputDir, String dbpSparqlEndpoint) throws IOException {
        Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_47, StandardAnalyzer.STOP_WORDS_SET);
        Analyzer englishAnalyzer = new EnglishAnalyzer(Version.LUCENE_47, EnglishAnalyzer.getDefaultStopSet());
        BufferedReader reader = new BufferedReader(new FileReader(startFile));
        //skip header
        if (reader.ready()) {
            reader.readLine();
        }
        int numPages = 1;
        while (reader.ready()) {
            if (numPages % 50 == 0) {
                //dirty solution to limit request to wikipedia
                logger.info("Waiting for 30 seconds...");
                myWait(30);
            }
            JSONObject mainObject = new JSONObject();
            String line = reader.readLine();
            String[] lineValues = line.split("\t");
            mainObject.put("id", lineValues[0]);
            mainObject.put("title", lineValues[1]);
            mainObject.put("uri", lineValues[2]);
            logger.log(Level.INFO, "Processing {0}", lineValues[0]);
            logger.log(Level.INFO, "Make dir");
            String bookDirname = outputDir + "/" + lineValues[0];
            new File(bookDirname).mkdir();
            SimpleQueryOutRelation outq = new SimpleQueryOutRelation(dbpSparqlEndpoint, lineValues[2]);
            List<SimpleResult> outResults = outq.runQuery(false);
            JSONArray outLinks = new JSONArray();
            String text = null;
            String abs = null;
            for (SimpleResult r : outResults) {
                if (r.getPredicate().equals(PREDICATE_WIKIPAGE)) {
                    numPages++;
                    int t = 0;
                    while (text == null && t < MAX_ATTEMPT) {
                        String url = r.getObject();
                        try {
                            text = ArticleExtractor.getInstance().getText(new URL(url));
                            if (text != null) {
                                text = text.replace("[ edit ]", "");
                            }
                        } catch (BoilerpipeProcessingException ex) {
                            Logger.getLogger(DownloadEntityData.class.getName()).log(Level.WARNING, "Error to extract text", ex);
                            logger.warning("Re-try downloading...");
                            myWait(15);
                            t++;
                        }
                    }
                    if (text == null) {
                        logger.log(Level.WARNING, "No text for {0}", lineValues[0]);
                        text = "";
                    }
                }
                if (r.getPredicate().equals(PREDICATE_ABSTRACT)) {
                    if (r.getObject().length() > 3) {
                        //remove language tag
                        abs = r.getObject().substring(0, r.getObject().length() - 3);
                    }
                }
                JSONObject outobj = new JSONObject();
                outobj.put("subject", r.getSubject());
                outobj.put("predicate", r.getPredicate());
                outobj.put("object", r.getObject());
                outLinks.add(outobj);
            }
            if (text == null) {
                text = "";
            }
            if (abs != null) {
                text = abs + "\n" + text;
            }
            mainObject.put("outLinks", outLinks);
            SimpleQueryInRelation outi = new SimpleQueryInRelation(dbpSparqlEndpoint, lineValues[2]);
            List<SimpleResult> inResults = outi.runQuery(false);
            JSONArray inLinks = new JSONArray();
            for (SimpleResult r : inResults) {
                JSONObject outobj = new JSONObject();
                outobj.put("subject", r.getSubject());
                outobj.put("predicate", r.getPredicate());
                outobj.put("object", r.getObject());
                inLinks.add(outobj);
            }
            mainObject.put("inLinks", inLinks);
            if (text != null) {
                mainObject.put("text", text);
            }
            FileWriter writer = new FileWriter(bookDirname + "/" + lineValues[0] + ".json");
            writer.write(mainObject.toJSONString());
            writer.close();
            if (text != null) {
                writer = new FileWriter(bookDirname + "/" + lineValues[0] + ".text");
                writer.write(text);
                writer.close();
                Map<String, Integer> map = extractToken(text, standardAnalyzer);
                writeMapToFile(map, bookDirname + "/" + lineValues[0] + ".tokens");
                map = extractToken(text, englishAnalyzer);
                writeMapToFile(map, bookDirname + "/" + lineValues[0] + ".stems");
                writer.close();
            }
        }
        reader.close();
    }

    public static void main(String[] args) {
        if (args.length == 3) {
            try {
                DownloadEntityData dd = new DownloadEntityData();
                dd.download(args[0], args[1], args[2]);
            } catch (IOException ex) {
                Logger.getLogger(DownloadEntityData.class.getName()).log(Level.SEVERE, "Error in downloading data", ex);
            }
        } else {
            logger.log(Level.WARNING, "Number of arguments not valid {0}", args.length);
            System.exit(1);
        }
    }

    private void myWait(int sec) {
        //wait
        logger.info("Ronf...");
        long cm = System.currentTimeMillis();
        while ((System.currentTimeMillis() - cm) < sec * 1000) {

        }
        logger.info("Wake-up!");
    } */
}
