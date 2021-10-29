package com.skennedy.bixbite.lexing.model;

import org.apache.commons.lang3.StringUtils;

public enum TokenType {

    WHITESPACE(null),
    INT_LITERAL(null),
    IDENTIFIER(null),
    COMMENT(null),

    //Operators
    PLUS("+"),
    MINUS("-"),
    STAR("*"),
    SLASH("/"),
    PERCENT("%"),
    COLON(":"),
    EQUALS("="),
    EQUALS_EQUALS("=="),
    BANG_EQUALS("!="),
    GT(">"),
    GTEQ(">="),
    LT("<"),
    LTEQ("<="),

    //Blocks
    OPEN_PARENTHESIS("("),
    CLOSE_PARENTHESIS(")"),
    OPEN_CURLY_BRACE("{"),
    CLOSE_CURLY_BRACE("}"),

    //Keywords
    OR_KEYWORD("or"),
    AND_KEYWORD("and"),
    IF_KEYWORD("if"),
    ELSE_KEYWORD("else"),
    WHILE_KEYWORD("while"),
    FOR_KEYWORD("for"),
    TO_KEYWORD("to"),
    BY_KEYWORD("by"),
    MOD_KEYWORD("mod"),
    TRUE_KEYWORD("true"),
    FALSE_KEYWORD("false"),

    //Types
    INT_KEYWORD("Int"),
    NUM_KEYWORD("Num"),
    BOOL_KEYWORD("Bool"),
    CONST_KEYWORD("const"),

    //Intrinsics
    PRINT_INTR("print"),
    TYPEOF_INTR("typeof"),

    //Other
    BAD_TOKEN(null),
    EOF_TOKEN(null);

    private final String text;

    TokenType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static TokenType fromText(String text) {

        if (StringUtils.isBlank(text)) {
            return WHITESPACE;
        }

        for (TokenType tokenType : TokenType.values()) {

            if (text.equals(tokenType.getText())) {
                return tokenType;
            }
        }

        return IDENTIFIER;
    }
}
