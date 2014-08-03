package di.uniba.it.lodrecsys.graph.indexer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Statement;
import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
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

    public LODIndexer(PropertiesManager manager, ArrayListMultimap<String, Set<String>> trainingPosNeg,
                      Map<String, String> idUriMap) throws IOException {

        indexCreator(manager, idUriMap, getPositiveOnly(trainingPosNeg));
    }

    private Map<String, Set<String>> getPositiveOnly(ArrayListMultimap<String, Set<String>> trainingPosNeg) {
        Map<String, Set<String>> positiveOnly = new HashMap<>();

        for (String userID : trainingPosNeg.keySet()) {
            positiveOnly.put(userID, trainingPosNeg.get(userID).get(0));
        }

        return positiveOnly;

    }

    private void indexCreator(PropertiesManager manager, Map<String, String> idUriMap, Map<String, Set<String>> usersProfile)
            throws IOException {
        IndexWriter writer = null;

        try {
            Directory d = FSDirectory.open(new File("users_profile"));
            IndexWriterConfig indexConfig = new IndexWriterConfig(Version.LUCENE_47, new WhitespaceAnalyzer(Version.LUCENE_47));
            writer = new IndexWriter(d, indexConfig);

            for (String userID : usersProfile.keySet()) {
                StringBuilder docTextBuilder = new StringBuilder("");

                for (String itemID : usersProfile.get(userID)) {
                    List<Statement> resourceList = manager.getResourceProperties(idUriMap.get(itemID));

                    for (Statement stat : resourceList) {
                        docTextBuilder.append(stat.getPredicate().toString()).append(":").append(stat.getObject().toString()).append("\n");
                    }
                }

                Document newDocument = new Document();
                newDocument.add(new StringField("user_id", userID, Field.Store.YES));
                newDocument.add(new TextField("content", docTextBuilder.toString(), Field.Store.YES));
                writer.addDocument(newDocument);

            }
        } finally {
            if (writer != null)
                writer.close();
        }
    }


}
