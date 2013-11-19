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
