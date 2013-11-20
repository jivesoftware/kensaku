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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jivesoftware.os.kensaku.service.plugins.KensakuQueryBuilder;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;

public class POCAllFieldsBooleanQueryBuilder implements KensakuQueryBuilder<Query> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);

    @Override
    public Query buildQuery(KensakuQuery kensakuQuery) throws Exception {
        BooleanQuery booleanQuery = new BooleanQuery();

        Map<String, String> fields = mapper.readValue(kensakuQuery.query,
            new TypeReference<HashMap<String, String>>() {
        });

        for (Entry<String, String> field : fields.entrySet()) {
            TokenStream tokenStream = analyzer.tokenStream(field.getKey(), field.getValue());
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                booleanQuery.add(new TermQuery(new Term(field.getKey(), term)), Occur.MUST);
            }

        }
        return booleanQuery;
    }
}