package org.dooq.api;

import org.dooq.Key;

public class AbstractKey extends Key {

    public AbstractKey() {
    }

    public AbstractKey(String partitionName, Object value) {
        setPartitionKey(partitionName, value);
    }

    public AbstractKey(String partitionName, Object partitionValue, String sortName, Object sortValue) {
        setPartitionKey(partitionName, partitionValue);
        setSortingKey(sortName, sortValue);
    }

}
