package com.jivesoftware.os.kensaku.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class KensakuQuery {

    public final String tenantId;
    public final int firstResult;
    public final int numberOfResults;
    public final Map<String, String> fields;

    @JsonCreator
    public KensakuQuery(@JsonProperty("tenantId") String tenantId,
            @JsonProperty("firstResult") int firstResult,
            @JsonProperty("numberOfResults") int numberOfResults,
            @JsonProperty("fields") Map<String, String> fields) {
        this.tenantId = tenantId;
        this.firstResult = firstResult;
        this.numberOfResults = numberOfResults;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "KensakuQuery{" + "tenantId=" + tenantId + ", firstResult=" + firstResult + ", numberOfResults=" + numberOfResults + ", fields=" + fields + '}';
    }

}