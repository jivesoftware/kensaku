package com.jivesoftware.os.kensaku.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KensakuQuery {

    public final String tenantId;
    public final int firstResult;
    public final int numberOfResults;
    public final byte[] query;

    @JsonCreator
    public KensakuQuery(@JsonProperty("tenantId") String tenantId,
            @JsonProperty("firstResult") int firstResult,
            @JsonProperty("numberOfResults") int numberOfResults,
            @JsonProperty("query") byte[] query) {
        this.tenantId = tenantId;
        this.firstResult = firstResult;
        this.numberOfResults = numberOfResults;
        this.query = query;
    }

    @Override
    public String toString() {
        return "KensakuQuery{" + "tenantId=" + tenantId + ", firstResult=" + firstResult + ", numberOfResults=" + numberOfResults + ", query=" + query + '}';
    }

}