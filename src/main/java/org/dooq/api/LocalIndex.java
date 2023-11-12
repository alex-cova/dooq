package org.dooq.api;

import java.lang.annotation.*;

@Repeatable(LocalIndices.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalIndex {

    String name();

    String sortKey();

    String[] projections() default {};

    ProjectionMode projectionMode() default ProjectionMode.ALL;
}
