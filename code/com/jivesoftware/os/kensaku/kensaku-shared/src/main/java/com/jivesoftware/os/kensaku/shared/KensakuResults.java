package com.jivesoftware.os.kensaku.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class KensakuResults {

    public final int firstResultOffest;
    public final int hitCount;
    public final List<KensakuResult> results;

    @JsonCreator
    public KensakuResults(@JsonProperty("firstResultOffest") int firstResultOffest,
            @JsonProperty("hitCount") int hitCount,
            @JsonProperty("results") List<KensakuResult> results) {
        this.firstResultOffest = firstResultOffest;
        this.hitCount = hitCount;
        this.results = results;
    }

    @Override
    public String toString() {
        return "KensakuResults{" + "firstResultOffest=" + firstResultOffest + ", hitCount=" + hitCount + ", results=" + results + '}';
    }
}