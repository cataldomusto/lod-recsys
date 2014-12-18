package di.uniba.it.lodrecsys.graph.indexer;

import com.google.common.collect.ArrayListMultimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by asuglia on 8/3/14.
 */
public class LODIndexer {
    static Logger currLogger = Logger.getLogger(LODIndexer.class);

    public static void createLODIndexer(PropertiesManager manager, ArrayListMultimap<String, Set<String>> trainingPosNeg,
                      Map<String, String> idUriMap) throws IOException {

        indexCreator(manager, idUriMap, getPositiveOnly(trainingPosNeg));
    }

    private static Map<String, Set<String>> getPositiveOnly(ArrayListMultimap<String, Set<String>> trainingPosNeg) {
        Map<String, Set<String>> positiveOnly = new HashMap<>();

        for (String userID : trainingPosNeg.keySet()) {
            positiveOnly.put(userID, trainingPosNeg.get(userID).get(0));
        }

        return positiveOnly;

    }

    private static void indexCreator(PropertiesManager manager, Map<String, String> idUriMap, Map<String, Set<String>> usersProfile)
            throws IOException {
        IndexWriter writer = null;


        try {
            Directory d = FSDirectory.open(new File("lod_index"));
            IndexWriterConfig indexConfig = new IndexWriterConfig(Version.LUCENE_47, new WhitespaceAnalyzer(Version.LUCENE_47));
            writer = new IndexWriter(d, indexConfig);

            for (String userID : usersProfile.keySet()) {
                StringBuilder docTextBuilder = new StringBuilder("");

                for (String itemID : usersProfile.get(userID)) {
                    List<Statement> resourceList = manager.getResourceProperties(idUriMap.get(itemID));

                    for (Statement stat : resourceList) {
                        //docTextBuilder.append(stat.getObject().toString()).append(" ");
                        docTextBuilder.append(stat.getPredicate().toString()).append(":").append(stat.getObject().toString()).append(" ");
                    }
                }

                Document newDocument = new Document();
                newDocument.add(new StringField("user_id", userID, Field.Store.YES));
                newDocument.add(new TextField("content", docTextBuilder.toString(), Field.Store.YES));
                writer.addDocument(newDocument);
                currLogger.info("Added document for user: " + userID);

            }

            for (String itemID : idUriMap.keySet()) {
                StringBuilder docTextBuilder = new StringBuilder("");
                List<Statement> resourceList = manager.getResourceProperties(idUriMap.get(itemID));

                for (Statement stat : resourceList) {
                    //docTextBuilder.append(stat.getObject().toString()).append(" ");
                    docTextBuilder.append(stat.getPredicate().toString()).append(":").append(stat.getObject().toString()).append(" ");
                }


                Document newDocument = new Document();
                newDocument.add(new StringField("item_id", itemID, Field.Store.YES));
                newDocument.add(new TextField("content", docTextBuilder.toString(), Field.Store.YES));
                writer.addDocument(newDocument);

                currLogger.info("Added document for item: " + itemID);

            }

        } finally {
            if (writer != null)
                writer.close();
        }
    }

}
