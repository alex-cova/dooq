package org.dooq.core.exception;

import org.jetbrains.annotations.NotNull;

import java.util.function.LongSupplier;

public class DynamoOperationException extends RuntimeException implements LongSupplier {

    public DynamoOperationException(@NotNull Object operation, Throwable cause) {
        super(operation.toString(), cause);
    }

    public DynamoOperationException(String message) {
        super(message);
    }

    @Override
    public long getAsLong() {
        return 500;
    }
}
