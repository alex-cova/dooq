package org.dooq.util;

import org.jetbrains.annotations.Nullable;

/**
 * Utility record to specify that a field can be set nullable
 *
 * @param value
 * @param <T>
 */
public record NullableValue<T>(@Nullable T value) {

    public boolean isNull() {
        return value == null;
    }
}
