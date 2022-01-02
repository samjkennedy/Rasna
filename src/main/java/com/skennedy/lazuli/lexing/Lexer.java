package com.skennedy.lazuli.lexing;

import com.skennedy.lazuli.lexing.model.Location;
import com.skennedy.lazuli.lexing.model.Token;
import com.skennedy.lazuli.lexing.model.TokenType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private static final char STRING_ESCAPE_CHAR = '\\';
    private int cursor;

    public List<Token> lex(String program) {

        List<Token> tokens = new ArrayList<>();

        int lineNumber = 0;
        for (String line : program.split(StringUtils.LF)) {
            if (StringUtils.isBlank(line)) {
                tokens.add(new Token(TokenType.WHITESPACE, new Location(lineNumber, 0)));
                lineNumber++;
                continue;
            }
            cursor = 0;
            do {
                if (Character.isWhitespace(current(line))) {
                    int start = cursor;
                    while (Character.isWhitespace(current(line))) {
                        next();
                    }
                    tokens.add(new Token(TokenType.WHITESPACE, new Location(lineNumber, start)));
                } else if (Character.isDigit(current(line))) {

                    tokens.add(new Token(TokenType.INT_LITERAL, new Location(lineNumber, cursor), parseNum(line)));

                } else if (Character.isAlphabetic(current(line)) || current(line) == '_') {
                    int start = cursor;

                    next();
                    while (Character.isAlphabetic(current(line)) || Character.isDigit(current(line)) || current(line) == '_') {
                        next();
                    }

                    //TODO: No span
                    String tokenText = line.substring(start, cursor);
                    TokenType tokenType = TokenType.fromText(tokenText);
                    if (tokenType == TokenType.IDENTIFIER) {
                        //TODO: This feels like an inelegant way to deal with identifiers
                        tokens.add(new Token(tokenType, new Location(lineNumber, start), tokenText));
                    } else if (tokenType == TokenType.TRUE_KEYWORD) {
                        tokens.add(new Token(tokenType, new Location(lineNumber, start), true));
                    } else if (tokenType == TokenType.FALSE_KEYWORD) {
                        tokens.add(new Token(tokenType, new Location(lineNumber, start), false));
                    } else {
                        tokens.add(new Token(tokenType, new Location(lineNumber, start)));
                    }

                } else {
                    switch (current(line)) {
                        case '+':

                            if (lookAhead(line) == '+') {
                                tokens.add(new Token(TokenType.INCREMENT, new Location(lineNumber, cursor)));
                                next();
                            } else {
                                tokens.add(new Token(TokenType.PLUS, new Location(lineNumber, cursor)));
                            }
                            next();
                            break;
                        case '-':
                            if (lookAhead(line) == '-') {
                                tokens.add(new Token(TokenType.DECREMENT, new Location(lineNumber, cursor)));
                                next();
                            } else if (lookAhead(line) == '>') {
                                tokens.add(new Token(TokenType.ARROW, new Location(lineNumber, cursor)));
                                next();
                            } else if (Character.isDigit(lookAhead(line))) {
                                tokens.add(new Token(TokenType.INT_LITERAL, new Location(lineNumber, cursor), parseNum(line)));
                                break;
                            } else {
                                tokens.add(new Token(TokenType.MINUS, new Location(lineNumber, cursor)));
                                next();
                            }
                            next();
                            break;
                        case '*':
                            tokens.add(new Token(TokenType.STAR, new Location(lineNumber, cursor)));
                            next();
                            break;
                        case '/':
                            if (lookAhead(line) == '/') {
                                int start = cursor;
                                cursor = line.length();
                                String comment = line.substring(start, cursor);
                                tokens.add(new Token(TokenType.COMMENT, new Location(lineNumber, cursor), comment));
                            } else {
                                tokens.add(new Token(TokenType.SLASH, new Location(lineNumber, cursor)));
                            }
                            next();
                            break;
                        case '%':
                            tokens.add(new Token(TokenType.PERCENT, new Location(lineNumber, cursor)));
                            next();
                            break;
                        case '|':
                            tokens.add(new Token(TokenType.BAR, new Location(lineNumber, cursor)));
                            next();
                            break;
                        case '>':
                            if (lookAhead(line) == '=') {
                                tokens.add(new Token(TokenType.GTEQ, new Location(lineNumber, cursor)));
                                next();
                            } else {
                                tokens.add(new Token(TokenType.GT, new Location(lineNumber, cursor)));
                            }
                            next();
                            break;
                        case '<':
                            if (lookAhead(line) == '=') {
                                tokens.add(new Token(TokenType.LTEQ, new Location(lineNumber, cursor)));
                                next();
                            } else {
                                tokens.add(new Token(TokenType.LT, new Location(lineNumber, cursor)));
                            }
                            next();
                            break;
                        case '=':
                            if (lookAhead(line) == '=') {
                                tokens.add(new Token(TokenType.EQUALS_EQUALS, new Location(lineNumber, cursor)));
                                next();
                            } else {
                                tokens.add(new Token(TokenType.EQUALS, new Location(lineNumber, cursor)));
                            }
                            next();
                        case '!':
                            if (lookAhead(line) == '=') {
                                tokens.add(new Token(TokenType.BANG_EQUALS, new Location(lineNumber, cursor)));
                                cursor += 2;
                            } else {
                                cursor++;
                            }
                            break;
                        case '(':
                            tokens.add(new Token(TokenType.OPEN_PARENTHESIS, new Location(lineNumber, cursor)));
                            next();
                            break;
                        case ')':
                            tokens.add(new Token(TokenType.CLOSE_PARENTHESIS, new Location(lineNumber, cursor)));
                            next();
                            break;
                        case '{':
                            tokens.add(new Token(TokenType.OPEN_CURLY_BRACE, new Location(lineNumber, cursor)));
                            next();
                            break;
                        case '}':
                            tokens.add(new Token(TokenType.CLOSE_CURLY_BRACE, new Location(lineNumber, cursor)));
                            next();
                            break;
                        case '[':
                            tokens.add(new Token(TokenType.OPEN_SQUARE_BRACE, new Location(lineNumber, cursor)));
                            next();
                            break;
                        case ']':
                            tokens.add(new Token(TokenType.CLOSE_SQUARE_BRACE, new Location(lineNumber, cursor)));
                            next();
                            break;
                        case ',':
                            tokens.add(new Token(TokenType.COMMA, new Location(lineNumber, cursor)));
                            next();
                            break;
                        case '"':
                            tokens.add(new Token(TokenType.STRING_LITERAL, new Location(lineNumber, cursor), parseString(line)));
                            break;
                        case '.':
                            tokens.add(new Token(TokenType.DOT, new Location(lineNumber, cursor)));
                            next();
                            break;
                        default:
                            tokens.add(new Token(TokenType.BAD_TOKEN, new Location(lineNumber, cursor)));
                            next();
                    }
                }
            } while (cursor < line.length());

            lineNumber++;
        }
        tokens.add(new Token(TokenType.EOF_TOKEN, new Location(lineNumber + 1, 0)));
        return tokens;
    }

    //TODO: Multi-line Strings
    private String parseString(String line) {

        next(); //Skip opening '"'
        int start = cursor;

        while (cursor < line.length() && (line.charAt(cursor) != '"' || line.charAt(cursor - 1) == STRING_ESCAPE_CHAR)) {
            next();
        }
        next();//Skip closing '"'
        return StringUtils.remove(line.substring(start, cursor - 1), STRING_ESCAPE_CHAR);
    }

    private Number parseNum(String line) {

        int start = cursor;

        next();
        while (cursor < line.length() && (Character.isDigit(line.charAt(cursor)) || line.charAt(cursor) == '.')) {
            next();
        }

        String tokenText = line.substring(start, cursor);

        if (tokenText.contains(".")) {
            try {
                return Double.parseDouble(tokenText);
            } catch (NumberFormatException nfe) {
                throw new IllegalStateException("Tried to parse Double but got " + tokenText + " at " + start);
            }
        }
        try {
            return Integer.parseInt(tokenText);
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException("Tried to parse Int but got " + tokenText + " at " + start);
        }
    }

    //TODO: Can only lookahead on one line, what if an expr is multiline?

    private char current(String line) {
        return peek(line, 0);
    }

    private char lookAhead(String line) {
        return peek(line, 1);
    }

    private char peek(String line, int offset) {
        int index = cursor + offset;

        if (index >= line.length()) {
            return '\0';
        }
        return line.charAt(index);
    }

    private void next() {
        cursor++;
    }
}
