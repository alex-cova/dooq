package com.dooq.core;

import com.dooq.api.Column;

public interface Expression {

    Column<?, ?> column();

    String render();
}
