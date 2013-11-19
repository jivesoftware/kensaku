package com.jivesoftware.os.kensaku.service;

import com.jivesoftware.os.kensaku.shared.KensakuQuery;
import com.jivesoftware.os.kensaku.shared.KensakuResult;
import com.jivesoftware.os.kensaku.shared.KensakuResults;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KensakuMergeResults {

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
                return -Float.compare(o1.score, o2.score);
            }
        });
        topResults = topResults.subList(0, Math.min(topResults.size(), kensakuQuery.numberOfResults));
    }

    KensakuResults getResults() {
        return new KensakuResults(kensakuQuery.firstResult, hitCount, topResults);
    }

}
