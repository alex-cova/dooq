package org.dooq;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ProductKey extends Key {

    public static @NotNull ProductKey of(long contentId, String productUuid) {
        var map = new ProductKey();

        map.setPartitionKey(Tables.PRODUCT.CONTENTID, contentId);
        map.setSortingKey(Tables.PRODUCT.UUID, productUuid);

        return map;
    }

    public static List<ProductKey> bulk(long contentId, @NotNull List<String> ids) {
        return ids.stream()
                .map(k -> of(contentId, k))
                .collect(Collectors.toList());
    }

}
