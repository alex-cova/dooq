package org.dooq.join;

import org.dooq.Key;
import org.dooq.api.AbstractRecord;
import org.dooq.api.Column;
import org.dooq.engine.ExpressionRenderer;
import org.dooq.engine.RendererContext;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

public class JoinExpression<R extends AbstractRecord<R>, K extends Key> implements ExpressionRenderer<R, K> {

    private boolean samePartition;

    public JoinExpression<R, K> samePartition() {
        this.samePartition = true;
        return this;
    }

    @Override
    public List<Column<R, K>> columns() {
        return null;
    }

    @Override
    public void render(RendererContext<R, K> context) {

    }

    @Override
    public boolean containsIndex(@Nullable Column<R, K> indexName) {
        return false;
    }

    @Override
    public Map<Column<R, K>, Object> getValue() {
        return null;
    }

    @Override
    public Map<String, AttributeValue> getAttributeValues() {
        return null;
    }

    @Override
    public Map<String, String> getAttributeNames() {
        return null;
    }
}