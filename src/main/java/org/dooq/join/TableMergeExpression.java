package org.dooq.join;

import org.dooq.api.Table;

public record TableMergeExpression(Table<?, ?> table) implements JoinExpression {
}
