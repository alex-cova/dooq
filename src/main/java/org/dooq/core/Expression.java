package org.dooq.core;

import org.dooq.api.Column;

public interface Expression {

    Column<?, ?> column();

    String render();
}
