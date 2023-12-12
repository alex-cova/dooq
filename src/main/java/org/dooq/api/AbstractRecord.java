package org.dooq.api;

import org.dooq.Key;

public class AbstractRecord extends DynamoRecord<AbstractRecord> {

    private Key key;

    @Override
    public Key getKey() {
        return key;
    }
}
