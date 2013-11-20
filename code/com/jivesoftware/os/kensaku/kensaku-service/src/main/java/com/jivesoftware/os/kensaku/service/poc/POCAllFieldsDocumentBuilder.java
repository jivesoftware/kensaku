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

import com.jivesoftware.os.kensaku.service.plugins.KensakuDocumentBuilder;
import com.jivesoftware.os.kensaku.shared.KensakuDocument;
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