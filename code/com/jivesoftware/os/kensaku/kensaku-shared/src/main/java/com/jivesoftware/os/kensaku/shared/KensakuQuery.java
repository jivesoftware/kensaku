/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.kensaku.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KensakuQuery {

    public final String tenantId;
    public final int firstResult;
    public final int numberOfResults;
    public final byte[] query;

    @JsonCreator
    public KensakuQuery(@JsonProperty("tenantId") String tenantId,
            @JsonProperty("firstResult") int firstResult,
            @JsonProperty("numberOfResults") int numberOfResults,
            @JsonProperty("query") byte[] query) {
        this.tenantId = tenantId;
        this.firstResult = firstResult;
        this.numberOfResults = numberOfResults;
        this.query = query;
    }

    @Override
    public String toString() {
        return "KensakuQuery{" + "tenantId=" + tenantId + ", firstResult=" + firstResult + ", numberOfResults=" + numberOfResults + ", query=" + query + '}';
    }

}