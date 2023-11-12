package org.dooq.lazy;

import org.dooq.DynamoSL;
import org.dooq.Key;
import org.dooq.api.Table;
import org.dooq.core.ItemParser;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LazyRecord implements InvocationHandler {

    private final DynamoSL dsl;
    private final Key key;
    private final Table<?, ?> table;
    public Object record;

    public LazyRecord(DynamoSL dsl, Table<?, ?> table, Key key) {
        this.dsl = dsl;
        this.table = table;
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull T newProxy(DynamoSL dsl, Table<?, ?> table, Key key, @NotNull Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new LazyRecord(dsl, table, key));
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {

        if (record == null) {
            var itemResponse = dsl.asClient()
                    .getItem(a -> a.tableName(table.getTableName())
                            .key(key));

            record = ItemParser.readRecord(itemResponse.item(), table.getRecordType());
        }

        return method.invoke(record, args);
    }
}
