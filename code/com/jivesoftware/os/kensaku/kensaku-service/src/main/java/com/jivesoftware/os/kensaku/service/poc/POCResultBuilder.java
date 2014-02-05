/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.kensaku.service.poc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jivesoftware.os.kensaku.service.plugins.KensakuResultBuilder;
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

public class POCResultBuilder implements KensakuResultBuilder<IndexSearcher, TopDocs> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final Collection<String> fetchFields;

    public POCResultBuilder(Collection<String> fetchFields) {
        this.fetchFields = fetchFields;
    }

    @Override
    public KensakuResults create(IndexSearcher searcher, TopDocs hits, int firstResultOffest, int numberOfResults) {
        List<KensakuResult> results = new ArrayList<>();
        for (int i = 0; i < hits.scoreDocs.length && i < numberOfResults; i++) {

            try {
                Map<String, String> fields = new HashMap<>();
                Document doc = searcher.doc(hits.scoreDocs[i].doc);
                for (String field : fetchFields) {
                    String value = doc.get(field);
                    fields.put(field, value);
                }
                Map<String, byte[]> payloads = new HashMap<>();
                payloads.put("score", mapper.writeValueAsBytes(hits.scoreDocs[i].score));
                payloads.put("fields", mapper.writeValueAsBytes(fields));

                results.add(new KensakuResult(payloads));
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
        return new KensakuResults(firstResultOffest, hits.totalHits, results);
    }
}
