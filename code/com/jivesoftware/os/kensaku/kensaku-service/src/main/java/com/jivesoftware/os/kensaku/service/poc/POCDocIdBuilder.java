package com.jivesoftware.os.kensaku.service.poc;

import com.jivesoftware.os.kensaku.service.plugins.KensakuDocIdBuilder;
import com.jivesoftware.os.kensaku.shared.KensakuDocument;
import org.apache.lucene.index.Term;

public class POCDocIdBuilder implements KensakuDocIdBuilder<Term> {

    @Override
    public Term build(KensakuDocument kensakuDocument) throws Exception {
        return new Term("docId", Long.toString(kensakuDocument.documentId));
    }
}
