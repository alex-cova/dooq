package org.dooq;

import org.dooq.annot.Lazy;

import java.util.List;

public class LazyRecord {

    String id;

    String name;

    @Lazy(value = "id", table = "likes")
    List<String> likes;

}
