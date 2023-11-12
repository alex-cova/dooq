package org.dooq.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LocalIndices {

    LocalIndex[] value();
}
