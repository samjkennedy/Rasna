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
    LXOR("Logical XOR"),
    MOD("modulo"),
    MUL("multiply"),
    NEG("negation"),
    NEQ("not equals"),
    NOT("logical not"),
    SUB("subtract");

    private final String name;

    OpType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
