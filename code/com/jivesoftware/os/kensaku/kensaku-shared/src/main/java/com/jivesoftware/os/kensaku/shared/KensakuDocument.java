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
package com.jivesoftware.os.kensaku.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class KensakuDocument {

    public final String tenantId;
    public final long documentId;
    public final long versionId;
    public final Map<String, List<String>> fields;

    @JsonCreator
    public KensakuDocument(@JsonProperty("tenantId") String tenantId,
            @JsonProperty("documentId") long documentId,
            @JsonProperty("versionId") long versionId,
            @JsonProperty("fields") Map<String, List<String>> fields) {
        this.tenantId = tenantId;
        this.documentId = documentId;
        this.versionId = versionId;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "KensakuDocument{" + "tenantId=" + tenantId + ", documentId=" + documentId + ", versionId=" + versionId + ", fields=" + fields + '}';
    }

}
