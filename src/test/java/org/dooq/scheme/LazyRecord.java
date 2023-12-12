package org.dooq.scheme;

import lombok.Data;
import org.dooq.annot.Lazy;

import java.util.List;

@Data
public class LazyRecord {

    private String id;
    private String name;
    @Lazy(value = "id", table = "likes")
    private List<String> likes;

}
