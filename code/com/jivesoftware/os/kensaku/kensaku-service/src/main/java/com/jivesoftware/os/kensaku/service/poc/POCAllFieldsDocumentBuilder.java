package com.jivesoftware.os.kensaku.service.poc;

import com.jivesoftware.os.kensaku.service.plugins.KensakuDocumentBuilder;
import com.jivesoftware.os.kensaku.shared.KensakuDocument;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;

public class POCAllFieldsDocumentBuilder implements KensakuDocumentBuilder<Document> {

    @Override
    public Document build(KensakuDocument kensakuDocument) throws Exception {
        Document doc = new Document();
        Field docId = new LongField("docId", kensakuDocument.documentId, Field.Store.YES);
        doc.add(docId);
        Field versionId = new LongField("versionId", kensakuDocument.versionId, Field.Store.YES);
        doc.add(versionId);
        for (Map.Entry<String, List<String>> field : kensakuDocument.fields.entrySet()) {
            String fieldString = field.getValue().get(0);
            TextField textField = new TextField(field.getKey(),
                    fieldString, Field.Store.YES);
            doc.add(textField);
        }
        return doc;
    }

}