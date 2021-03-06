package com.skennedy.rasna.diagnostics;

import com.skennedy.rasna.lexing.model.Location;
import com.skennedy.rasna.lexing.model.Token;
import com.skennedy.rasna.lexing.model.TokenType;

import java.nio.file.Path;

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
        return new Error("Expected " + expected + " but got " + actual.getTokenType() + " ", actual, actual.getLocation());
    }

    public static Error raiseUnexpectedToken(Token token) {
        return new Error("Unexpected token: " + token.getTokenType() + " ", token, token.getLocation());
    }

    public static Error raiseImportError(Path path, Token token) {
        return new Error("Cannot resolve import " + path.getFileName() + " ", token, token.getLocation());
    }

    public static Error raise(String message, Token token) {
        return new Error(message, token, token.getLocation());
    }

    public static Error raiseUnexpectedTokenAtTopLevel(TokenType unexpected, Token token) {
        return new Error("Unexpected token `" + unexpected + "` at top level: ", token, token.getLocation());
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
