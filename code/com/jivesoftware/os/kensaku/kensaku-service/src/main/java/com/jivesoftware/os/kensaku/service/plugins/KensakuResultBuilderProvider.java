package com.jivesoftware.os.kensaku.service.plugins;

import com.jivesoftware.os.kensaku.service.poc.POCResultBuilder;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;

public interface KensakuResultBuilderProvider {

    POCResultBuilder create(KensakuQuery kensakuQuery) throws Exception;
}
