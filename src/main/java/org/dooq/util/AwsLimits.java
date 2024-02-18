package org.dooq.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * AWS limits
 */
public class AwsLimits {

    public static int MAX_BATCH_PUT_SIZE = 25;
    public static int MAX_BATCH_DELETE_SIZE = 25;
    public static int MAX_BATCH_GET_SIZE = 100;


    public static <T, R> @NotNull List<R> paginate(@NotNull List<T> list, int pageSize, Function<List<T>, R> consumer) {

        if (list.isEmpty()) return Collections.emptyList();

        if (list.size() <= pageSize) {
            return Collections.singletonList(consumer.apply(list));
        }

        var results = new ArrayList<R>();

        int fromIndex = 0;
        int toIndex = pageSize;

        while (fromIndex < list.size()) {
            if (toIndex > list.size()) {
                toIndex = list.size();
            }

            results.add(consumer.apply(list.subList(fromIndex, toIndex)));

            fromIndex = toIndex;

            toIndex += pageSize;
        }

        return results;
    }
}
