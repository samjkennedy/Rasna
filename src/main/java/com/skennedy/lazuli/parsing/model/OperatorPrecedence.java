package com.skennedy.lazuli.parsing.model;

import com.skennedy.lazuli.lexing.model.Token;

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
            case IS_KEYWORD:
            case EQUALS_EQUALS:
            case BANG_EQUALS:
                return 4;
            case CLOSE_ANGLE_BRACE:
            case OPEN_ANGLE_BRACE:
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
