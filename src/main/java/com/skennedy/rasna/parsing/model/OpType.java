package com.skennedy.rasna.parsing.model;

public enum OpType {
    ADD("add"),
    DIV("divide"),
    EQ("equals"),
    GT("greater than"),
    GTEQ("greater than or equal"),
    INC("increment"),
    LAND("logical and"),
    LOR("logical or"),
    LT("less than"),
    LTEQ("less than or equal"),
    MOD("modulo"),
    MUL("multiply"),
    NEQ("not equals"),
    NOT("logical not"),
    NEG("negation"),
    SUB("subtract");

    private final String name;

    OpType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
