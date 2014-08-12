package di.uniba.it.lodrecsys.graph.indexer;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queries.mlt.MoreLikeThisQuery;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by asuglia on 8/3/14.
 */
public class LODIndexerReader {
    private IndexSearcher indexSearcher;

    public LODIndexerReader(String indexDir) throws IOException {

        indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(indexDir))));

    }

    public Map<String, Double> computeSimilarityUser(String currUser) throws IOException {
        int currUserDoc = getUserProfileDocID(currUser);
        Map<String, Double> scoreMap = new HashMap<>();
        //Document currUserProfile = indexSearcher.doc(currUserDoc);

        Query userProfileQuery = getUserProfileQuery(currUserDoc);//getUserProfileQuery(currUserProfile);
        TopDocs docs = indexSearcher.search(userProfileQuery, indexSearcher.getIndexReader().numDocs());
        double maxScore = Double.MIN_VALUE;

        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            if (maxScore <= scoreDoc.score)
                maxScore = scoreDoc.score;
            scoreMap.put(getEntityIDFromLuceneID(scoreDoc.doc), (double) scoreDoc.score);
        }

        // Normalize the lucene score between 0-1 using the maximum score for the current user
        for (String id : scoreMap.keySet()) {
            scoreMap.put(id, scoreMap.get(id) / maxScore);
        }

        return scoreMap;

    }

    private String getEntityIDFromLuceneID(int luceneDocID) throws IOException {
        Document document = indexSearcher.doc(luceneDocID);

        IndexableField currField = document.getField("user_id");

        if (currField == null)
            currField = document.getField("item_id");

        TokenStream stream = currField.tokenStream(new WhitespaceAnalyzer(Version.LUCENE_47));

        stream.reset();
        String entityID = null;

        while (stream.incrementToken()) {
            entityID = stream.getAttribute(CharTermAttribute.class).toString();
        }

        stream.close();

        return (currField.name().equals("item_id")) ? "I:" + entityID : "U:" + entityID;
    }

    private int getUserProfileDocID(String realUserID) throws IOException {
        Query userQuery = new TermQuery(new Term("user_id", realUserID));
        TopDocs profiles = indexSearcher.search(userQuery, 1);

        return profiles.scoreDocs[0].doc;

    }

    private Query getUserProfileQuery(int docNum) throws IOException {
        MoreLikeThis mlt = new MoreLikeThis(indexSearcher.getIndexReader());
        mlt.setAnalyzer(new WhitespaceAnalyzer(Version.LUCENE_47));
        mlt.setFieldNames(new String[]{"content"});

        return mlt.like(docNum);
    }


}
