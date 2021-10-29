package com.skennedy.bixbite.parsing.model;

import com.skennedy.bixbite.lexing.model.Token;

public class OperatorPrecedence {
    
    private OperatorPrecedence() {
        
    }

    //TODO: This should be OpType not token
    public static int getBinaryOperatorPrecedence(Token token) {
        switch (token.getTokenType()) {
            case STAR:
            case SLASH:
            case PERCENT:
            case MOD_KEYWORD:
                return 6;
            case PLUS:
            case MINUS:
                return 5;
            case EQUALS_EQUALS:
            case BANG_EQUALS:
                return 4;
            case GT:
            case LT:
            case GTEQ:
            case LTEQ:
                return 3;
            case AND_KEYWORD:
                return 2;
            case OR_KEYWORD:
                return 1;
            default:
                return 0;
        }
    }

    public static int getUnaryOperatorPrecedence(Token token) {
        switch (token.getTokenType()) {
            case PLUS:
            case MINUS:
                return 1;
            default:
                return 0;
        }
    }
}
