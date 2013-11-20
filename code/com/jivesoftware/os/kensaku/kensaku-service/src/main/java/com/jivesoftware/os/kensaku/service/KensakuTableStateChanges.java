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
