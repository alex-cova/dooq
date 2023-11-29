package org.dooq.api;

public interface FieldType<T> {

    Class<T> type();

    String name();


}
