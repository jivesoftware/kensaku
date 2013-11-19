package com.jivesoftware.os.kensaku.service.plugins;

import com.jivesoftware.os.kensaku.shared.KensakuDocument;

public interface KensakuDocumentBuilder<D> {

    D build(KensakuDocument kensakuDocument) throws Exception;
}
