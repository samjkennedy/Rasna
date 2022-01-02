package com.skennedy.lazuli.diagnostics;

import com.skennedy.lazuli.lexing.model.TokenType;
import com.skennedy.lazuli.lexing.model.Location;
import com.skennedy.lazuli.lexing.model.Token;
import com.skennedy.lazuli.typebinding.TypeSymbol;

public class Error {

    private final String message;
    private final Token token;
    private final Location location;

    private Error(String message, Token token, Location location) {
        this.message = message;
        this.token = token;
        this.location = location;
    }

    public static Error raiseUnexpectedToken(TokenType expected, Token actual) {
        return new Error("Expected " + expected + " but got " + actual.getTokenType(), actual, actual.getLocation());
    }

    public String getMessage() {
        return message;
    }

    public Token getToken() {
        return token;
    }

    public Location getLocation() {
        return location;
    }
}
