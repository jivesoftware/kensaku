package com.jivesoftware.os.kensaku.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class KensakuResult {

    public final float score;
    public final Map<String, String> fields;

    @JsonCreator
    public KensakuResult(@JsonProperty("score") float score,
            @JsonProperty("fields") Map<String, String> fields) {
        this.score = score;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "KensakuResult{" + "score=" + score + ", fields=" + fields + '}';
    }
}