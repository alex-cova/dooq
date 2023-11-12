package org.dooq;

import org.dooq.annot.JoinTarget;

import java.util.List;

public class JoinedRecord {

    String id;

    String name;

    @JoinTarget(value = "id", table = "mixer")
    List<String> likes;

}