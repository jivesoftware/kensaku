package com.jivesoftware.os.kensaku.service.poc;

import com.jivesoftware.os.kensaku.service.plugins.KensakuResultBuilderProvider;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;
import java.util.Arrays;

public class POCResultBuilderProvider implements KensakuResultBuilderProvider {

    @Override
    public POCResultBuilder create(KensakuQuery kensakuQuery) throws Exception {
        return new POCResultBuilder(Arrays.asList("docId", "body"));
    }
}