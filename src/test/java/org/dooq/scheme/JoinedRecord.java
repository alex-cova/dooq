package org.dooq.scheme;

import org.dooq.annot.JoinTarget;

import java.util.List;

public class JoinedRecord {

    String id;
    String name;

    @JoinTarget(value = "id", table = "mixer")
    List<String> likes;

}
