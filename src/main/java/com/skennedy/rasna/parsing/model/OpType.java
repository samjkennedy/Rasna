package com.skennedy.rasna.parsing.model;

public enum OpType {
    ADD("add"),
    SUB("subtract"),
    MUL("multiply"),
    DIV("divide"),
    MOD("modulo"),
    INC("increment"),
    EQ("equals"),
    NEQ("not equals"),
    GT("greater than"),
    LT("less than"),
    GTEQ("greater than or equal"),
    LTEQ("less than or equal"),
    LAND("logical and"),
    LOR("logical or");

    private final String name;

    OpType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
