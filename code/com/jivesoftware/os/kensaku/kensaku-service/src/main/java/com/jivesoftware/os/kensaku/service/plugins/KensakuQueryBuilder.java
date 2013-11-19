package com.jivesoftware.os.kensaku.service.plugins;

import com.jivesoftware.os.kensaku.shared.KensakuQuery;

/**
 *
 * @param <Q> query
 */
public interface KensakuQueryBuilder<Q> {

    Q buildQuery(KensakuQuery kensakuQuery) throws Exception;
}