package com.jivesoftware.os.kensaku.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;
import com.jivesoftware.os.kensaku.shared.KensakuResult;
import com.jivesoftware.os.kensaku.shared.KensakuResults;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KensakuMergeResults {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final KensakuQuery kensakuQuery;
    private int hitCount = 0;
    private List<KensakuResult> topResults = new ArrayList<>();

    public KensakuMergeResults(KensakuQuery kensakuQuery) {
        this.kensakuQuery = kensakuQuery;
    }

    void merge(KensakuResults kensakuResults) {

        hitCount += kensakuResults.hitCount;
        topResults.addAll(kensakuResults.results);
        Collections.sort(topResults, new Comparator<KensakuResult>() {
            @Override
            public int compare(KensakuResult o1, KensakuResult o2) {
                try {
                    return -Float.compare(mapper.readValue(o1.payloads.get("score"), Float.class),
                        mapper.readValue(o2.payloads.get("score"), Float.class));
                } catch (Exception x) {
                    x.printStackTrace();
                    return -1;
                }
            }
        });
        topResults = topResults.subList(0, Math.min(topResults.size(), kensakuQuery.numberOfResults));
    }

    KensakuResults getResults() {
        return new KensakuResults(kensakuQuery.firstResult, hitCount, topResults);
    }

    float floatFromBytes(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }
}
