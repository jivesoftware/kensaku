package com.jivesoftware.os.kensaku.service.poc;

import com.jivesoftware.os.kensaku.shared.KensakuResult;
import com.jivesoftware.os.kensaku.shared.KensakuResults;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

public class POCResultBuilder {

    private final Collection<String> fetchFields;

    public POCResultBuilder(Collection<String> fetchFields) {
        this.fetchFields = fetchFields;
    }

    public KensakuResults create(IndexSearcher searcher, TopDocs hits, int firstResultOffest, int numberOfResults) {
        List<KensakuResult> results = new ArrayList<>();
        for (int i = 0; i < hits.scoreDocs.length && i < numberOfResults; i++) {
            Map<String, String> fields = new HashMap<>();

            try {
                Document doc = searcher.doc(hits.scoreDocs[i].doc);
                for (String field : fetchFields) {
                    String value = doc.get(field);
                    fields.put(field, value);
                }
                results.add(new KensakuResult(hits.scoreDocs[i].score, fields));
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
        return new KensakuResults(firstResultOffest, hits.totalHits, results);
    }
}