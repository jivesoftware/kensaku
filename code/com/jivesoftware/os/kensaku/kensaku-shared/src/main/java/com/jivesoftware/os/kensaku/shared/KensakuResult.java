package com.jivesoftware.os.kensaku.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class KensakuResult {

    public final Map<String, byte[]> payloads;

    @JsonCreator
    public KensakuResult(@JsonProperty("payloads") Map<String, byte[]> payloads) {
        this.payloads = payloads;
    }

    @Override
    public String toString() {
        return "KensakuResult{ payloads=" + payloads + '}';
    }
}