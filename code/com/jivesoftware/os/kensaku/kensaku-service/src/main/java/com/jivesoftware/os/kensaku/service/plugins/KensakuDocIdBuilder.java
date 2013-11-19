package com.jivesoftware.os.kensaku.service.plugins;

import com.jivesoftware.os.kensaku.shared.KensakuDocument;

public interface KensakuDocIdBuilder<K> {

    K build(KensakuDocument kensakuDocument) throws Exception;
}
