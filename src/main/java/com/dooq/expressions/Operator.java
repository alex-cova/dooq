package com.dooq.expressions;

public enum Operator {

    AND("AND"),
    NOT("NOT"),
    OR("OR"),
    NONE("");

    private final String value;

    Operator(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isOperator(String value) {
        if (value.length() > 3) return false;

        return value.equalsIgnoreCase(AND.value)
                || value.equalsIgnoreCase(NOT.value)
                || value.equalsIgnoreCase(OR.value);
    }
}
