package org.dooq.scheme;

import org.dooq.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ProductKey extends Key {

    public static @NotNull ProductKey of(long companyId, String productUuid) {
        var map = new ProductKey();

        map.setPartitionKey(Tables.PRODUCT.COMPANYID, companyId);
        map.setSortingKey(Tables.PRODUCT.UUID, productUuid);

        return map;
    }

    public static List<ProductKey> bulk(long companyId, @NotNull List<String> ids) {
        return ids.stream()
                .map(k -> of(companyId, k))
                .collect(Collectors.toList());
    }

}
