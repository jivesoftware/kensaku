package com.jivesoftware.os.kensaku.service;

import com.jivesoftware.os.jive.utils.http.client.rest.RequestHelper;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;
import com.jivesoftware.os.kensaku.shared.KensakuResults;

public class KensakuClient {

    private final RequestHelper requestHelper;

    KensakuClient(RequestHelper requestHelper) {
        this.requestHelper = requestHelper;
    }

    public KensakuResults searchLocalPartition(int partition, KensakuQuery query) throws Exception {
        return requestHelper.executeRequest(query, "/kensaku/searchLocally?partitionId=" + partition, KensakuResults.class, null);
    }
}