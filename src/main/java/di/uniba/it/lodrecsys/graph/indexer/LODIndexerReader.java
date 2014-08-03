package di.uniba.it.lodrecsys.graph.indexer;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by asuglia on 8/3/14.
 */
public class LODIndexerReader {
    private IndexSearcher indexSearcher;

    public LODIndexerReader(String indexDir) throws IOException {

        indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(indexDir))));


    }

    public float computeSimilarityUser(String firstUser, String secUser) throws IOException {
        int firstUserDoc = getUserProfileDocID(firstUser), secUserDoc = getUserProfileDocID(secUser);

        Document secUserProfile = indexSearcher.doc(secUserDoc);

        Query secUserProfileQuery = getUserProfileQuery(secUserProfile);

        TopDocs results = indexSearcher.search(secUserProfileQuery, 10);

        for (ScoreDoc scoreDoc : results.scoreDocs) {
            if (scoreDoc.doc == firstUserDoc) {
                return scoreDoc.score;
            }
        }

        return 0;
    }

    private int getUserProfileDocID(String realUserID) throws IOException {
        Query userQuery = new TermQuery(new Term("user_id", realUserID));
        TopDocs profiles = indexSearcher.search(userQuery, 1);

        return profiles.scoreDocs[0].doc;

    }

    private Query getUserProfileQuery(Document userProfile) throws IOException {
        Query userProfileQuery = new BooleanQuery();

        for (IndexableField field : userProfile.getFields())
            if (field.name().equals("content")) {
                TokenStream stream = field.tokenStream(new WhitespaceAnalyzer(Version.LUCENE_47));

                while (stream.incrementToken()) {
                    ((BooleanQuery) userProfileQuery).add(new TermQuery(new Term("content", stream.getAttribute(CharTermAttribute.class).toString())), BooleanClause.Occur.SHOULD);
                }

            }


        return userProfileQuery;

    }

    public Double computeSimilarityUserItem(String userID, String itemID) {

        return 0d;
    }


}
