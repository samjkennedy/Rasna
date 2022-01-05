package com.skennedy.lazuli.lexing.model;

public class Token {

    private final TokenType tokenType;
    private final Location location;
    private final Object value;

    public Token(TokenType tokenType, Location location) {
        this(tokenType, location, tokenType.getText());
    }

    public Token(TokenType tokenType, Location location, Object value) {
        this.tokenType = tokenType;
        this.location = location;
        this.value = value;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public Location getLocation() {
        return location;
    }

    public boolean hasValue() {
        return value != null;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return tokenType.toString() + (hasValue() ? "<" + getValue() + ">" : "");
    }
}
