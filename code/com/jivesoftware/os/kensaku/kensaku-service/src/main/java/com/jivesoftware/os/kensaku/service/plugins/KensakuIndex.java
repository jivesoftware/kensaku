package com.jivesoftware.os.kensaku.service.plugins;

import com.jivesoftware.os.kensaku.shared.KensakuDocument;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;
import com.jivesoftware.os.kensaku.shared.KensakuResults;

public interface KensakuIndex<K> {

    void close();

    long count(KensakuQuery query) throws Exception;

    void index(KensakuDocument doc) throws Exception;

    void reIndex(KensakuDocument doc) throws Exception;

    void unIndex(K docId) throws Exception;

    KensakuResults search(KensakuQuery query) throws Exception;

    void truncate() throws Exception;

    void optimize() throws Exception;
}
