package org.dooq.join;

import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

public class MergeUtil {

    public static @NotNull Map<String, AttributeValue> merge(Map<String, AttributeValue> a, Map<String, AttributeValue> b) {

        var map = new HashMap<>(a);

        map.putAll(b);

        return map;
    }
}
