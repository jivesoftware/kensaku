package com.jivesoftware.os.kensaku.service.poc;

import com.jivesoftware.os.kensaku.service.plugins.KensakuQueryBuilder;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;
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

    private final StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);

    @Override
    public Query buildQuery(KensakuQuery kensakuQuery) throws Exception {
        BooleanQuery booleanQuery = new BooleanQuery();

        for (Entry<String, String> field : kensakuQuery.fields.entrySet()) {
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