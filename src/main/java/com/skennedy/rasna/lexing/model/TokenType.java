package com.skennedy.rasna.lexing.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public enum TokenType {

    WHITESPACE(null),
    LINE_BREAK("\n"),
    NUM_LITERAL(null),
    CHAR_LITERAL(null),
    STRING_LITERAL(null),
    IDENTIFIER(null),
    COMMENT(null),

    //Operators
    PLUS("+"),
    MINUS("-"),
    STAR("*"),
    SLASH("/"),
    PERCENT("%"),
    BAR("|"),
    EQUALS("="),
    EQUALS_EQUALS("=="),
    BANG_EQUALS("!="),
    CLOSE_ANGLE_BRACE(">"),
    GTEQ(">="),
    OPEN_ANGLE_BRACE("<"),
    LTEQ("<="),
    INCREMENT("++"),
    DECREMENT("--"),

    //Blocks
    OPEN_PARENTHESIS("("),
    CLOSE_PARENTHESIS(")"),
    OPEN_CURLY_BRACE("{"),
    CLOSE_CURLY_BRACE("}"),
    OPEN_SQUARE_BRACE("["),
    CLOSE_SQUARE_BRACE("]"),

    //Syntax
    COMMA(","),
    ARROW("->"),
    THICC_ARROW("=>"),
    DOT("."),
    COLON(":"),
    COLON_COLON("::"),

    //Keywords
    OR_KEYWORD("or"),
    AND_KEYWORD("and"),
    NOT_KEYWORD("not"),
    XOR_KEYWORD("xor"),
    IF_KEYWORD("if"),
    ELSE_KEYWORD("else"),
    WHILE_KEYWORD("while"),
    FOR_KEYWORD("for"),
    IN_KEYWORD("in"),
    TO_KEYWORD("to"),
    BY_KEYWORD("by"),
    YIELD_KEYWORD("yield"),
    MOD_KEYWORD("mod"),
    TRUE_KEYWORD("true"),
    FALSE_KEYWORD("false"),
    RETURN_KEYWORD("return"),
    MATCH_KEYWORD("match"),
    CONST_KEYWORD("const"),
    STRUCT_KEYWORD("struct"),
    FN_KEYWORD("fn"),
    IMPORT_KEYWORD("import"),
    INLINE_KEYWORD("inline"),
    NAMESPACE_KEYWORD("namespace"),
    AS_KEYWORD("as"),
    IS_KEYWORD("is"),
    REF_KEYWORD("ref"),
    ENUM_KEYWORD("enum"),

    //Types
    ANY_KEYWORD("Any"),
    BOOL_KEYWORD("Bool"),
    CHAR_KEYWORD("Char"),
    INT_KEYWORD("Int"),
    REAL_KEYWORD("Real"),
    STRING_KEYWORD("String"),
    FUNCTION_KEYWORD("Function"),

    //Intrinsics
    PRINT_INTR("print"),
    LEN_INTR("len"),
    TYPEOF_INTR("typeof"),
    MAP_INTR("map"),

    //Other
    BAD_TOKEN(null),
    EOF_TOKEN(null);

    private final String text;

    public static List<TokenType> typeTokens = Arrays.asList(
            BOOL_KEYWORD,
            INT_KEYWORD,
            REAL_KEYWORD,
            STRING_KEYWORD,
            FUNCTION_KEYWORD
    );

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
