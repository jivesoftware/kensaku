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
import java.util.List;

public class KensakuResults {

    public final int firstResultOffest;
    public final int hitCount;
    public final List<KensakuResult> results;

    @JsonCreator
    public KensakuResults(@JsonProperty("firstResultOffest") int firstResultOffest,
            @JsonProperty("hitCount") int hitCount,
            @JsonProperty("results") List<KensakuResult> results) {
        this.firstResultOffest = firstResultOffest;
        this.hitCount = hitCount;
        this.results = results;
    }

    @Override
    public String toString() {
        return "KensakuResults{" + "firstResultOffest=" + firstResultOffest + ", hitCount=" + hitCount + ", results=" + results + '}';
    }
}