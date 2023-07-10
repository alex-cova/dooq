package com.dooq.join;

import com.dooq.Key;
import com.dooq.api.AbstractRecord;
import com.dooq.api.Column;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class EqualsColumnExpression<R extends AbstractRecord<R>, K extends Key> extends JoinExpression<R, K> {

    private Column<?, ?> targetColumn;
    private Column<R, K> column;


    public EqualsColumnExpression(Column<?, ?> column, Column<R, K> otherColumn) {
        this.targetColumn = column;
        this.column = otherColumn;
    }


}
