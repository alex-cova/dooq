package org.dooq.join;

import org.jetbrains.annotations.NotNull;

public record FullMergeExpression<T, K>(@NotNull MergeExpression<T> expression1,
                                        @NotNull MergeExpression<K> expression2)
        implements JoinExpression {

}
