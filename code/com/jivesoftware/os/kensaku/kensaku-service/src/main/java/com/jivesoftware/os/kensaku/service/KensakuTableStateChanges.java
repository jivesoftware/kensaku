/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jivesoftware.os.kensaku.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jivesoftware.os.amza.shared.TableDelta;
import com.jivesoftware.os.amza.shared.TableName;
import com.jivesoftware.os.amza.shared.TableStateChanges;
import com.jivesoftware.os.amza.shared.TimestampedValue;
import com.jivesoftware.os.kensaku.service.plugins.KensakuIndex;
import com.jivesoftware.os.kensaku.shared.KensakuDocument;
import java.util.Map.Entry;

/**
 *
 * @author jonathan
 */
public class KensakuTableStateChanges implements TableStateChanges<Object, Object> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final KensakuIndexProvider kensakuIndexProvider;

    public KensakuTableStateChanges(KensakuIndexProvider kensakuIndexerProvider) {
        this.kensakuIndexProvider = kensakuIndexerProvider;
    }

    @Override
    public void changes(TableName<Object, Object> tableName, TableDelta<Object, Object> changes) throws Exception {

        if (tableName.getTableName().startsWith("index-")) {
            KensakuIndex kensakuIndex = kensakuIndexProvider.getKensakuIndex(tableName);
            for (Entry<Object, TimestampedValue<Object>> entry : changes.getApply().entrySet()) {
                if (!entry.getValue().getTombstoned()) {
                    KensakuDocument doc = mapper.readValue(entry.getValue().getValue().toString(), KensakuDocument.class);
                    kensakuIndex.index(doc);
                }
            }
            for (Entry<Object, TimestampedValue<Object>> entry : changes.getClobbered().entries()) {
                if (!entry.getValue().getTombstoned()) {
                    KensakuDocument doc = mapper.readValue(entry.getValue().getValue().toString(), KensakuDocument.class);
                    kensakuIndex.reIndex(doc);
                }
            }
            // TODO handle deletes need to add to PartitionDelta
        }
    }

}
