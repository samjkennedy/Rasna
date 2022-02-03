package com.skennedy.rasna.lexing;

import com.skennedy.rasna.lexing.model.Location;
import com.skennedy.rasna.lexing.model.Token;
import com.skennedy.rasna.lexing.model.TokenType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private static final char STRING_ESCAPE_CHAR = '\\';
    private int cursor;

    public List<Token> lex(String filePath, String program) {

        List<Token> tokens = new ArrayList<>();

        int lineNumber = 0;
        for (String line : program.split(StringUtils.LF)) {
            if (StringUtils.isBlank(line)) {
                tokens.add(new Token(TokenType.WHITESPACE, new Location(filePath, lineNumber, 0)));
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
                    tokens.add(new Token(TokenType.WHITESPACE, new Location(filePath, lineNumber, start)));
                } else if (Character.isDigit(current(line))) {

                    tokens.add(new Token(TokenType.NUM_LITERAL, new Location(filePath, lineNumber, cursor), parseNum(line)));

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
                        tokens.add(new Token(tokenType, new Location(filePath, lineNumber, start), tokenText));
                    } else if (tokenType == TokenType.TRUE_KEYWORD) {
                        tokens.add(new Token(tokenType, new Location(filePath, lineNumber, start), true));
                    } else if (tokenType == TokenType.FALSE_KEYWORD) {
                        tokens.add(new Token(tokenType, new Location(filePath, lineNumber, start), false));
                    } else {
                        tokens.add(new Token(tokenType, new Location(filePath, lineNumber, start)));
                    }

                } else {
                    switch (current(line)) {
                        case '+':

                            if (lookAhead(line) == '+') {
                                tokens.add(new Token(TokenType.INCREMENT, new Location(filePath, lineNumber, cursor)));
                                next();
                            } else {
                                tokens.add(new Token(TokenType.PLUS, new Location(filePath, lineNumber, cursor)));
                            }
                            next();
                            break;
                        case '-':
                            if (lookAhead(line) == '-') {
                                tokens.add(new Token(TokenType.DECREMENT, new Location(filePath, lineNumber, cursor)));
                                next();
                            } else if (lookAhead(line) == '>') {
                                tokens.add(new Token(TokenType.ARROW, new Location(filePath, lineNumber, cursor)));
                                next();
                            } else {
                                tokens.add(new Token(TokenType.MINUS, new Location(filePath, lineNumber, cursor)));
                            }
                            next();
                            break;
                        case '*':
                            tokens.add(new Token(TokenType.STAR, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case '/':
                            if (lookAhead(line) == '/') {
                                int start = cursor;
                                cursor = line.length();
                                String comment = line.substring(start, cursor);
                                tokens.add(new Token(TokenType.COMMENT, new Location(filePath, lineNumber, cursor), comment));
                            } else {
                                tokens.add(new Token(TokenType.SLASH, new Location(filePath, lineNumber, cursor)));
                            }
                            next();
                            break;
                        case '%':
                            tokens.add(new Token(TokenType.PERCENT, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case '|':
                            tokens.add(new Token(TokenType.BAR, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case '>':
                            if (lookAhead(line) == '=') {
                                tokens.add(new Token(TokenType.GTEQ, new Location(filePath, lineNumber, cursor)));
                                next();
                            } else {
                                tokens.add(new Token(TokenType.CLOSE_ANGLE_BRACE, new Location(filePath, lineNumber, cursor)));
                            }
                            next();
                            break;
                        case '<':
                            if (lookAhead(line) == '=') {
                                tokens.add(new Token(TokenType.LTEQ, new Location(filePath, lineNumber, cursor)));
                                next();
                            } else {
                                tokens.add(new Token(TokenType.OPEN_ANGLE_BRACE, new Location(filePath, lineNumber, cursor)));
                            }
                            next();
                            break;
                        case '=':
                            if (lookAhead(line) == '=') {
                                tokens.add(new Token(TokenType.EQUALS_EQUALS, new Location(filePath, lineNumber, cursor)));
                                next();
                            } else if (lookAhead(line) == '>') {
                                tokens.add(new Token(TokenType.THICC_ARROW, new Location(filePath, lineNumber, cursor)));
                                next();
                            } else {
                                tokens.add(new Token(TokenType.EQUALS, new Location(filePath, lineNumber, cursor)));
                            }
                            next();
                        case '!':
                            if (lookAhead(line) == '=') {
                                tokens.add(new Token(TokenType.BANG_EQUALS, new Location(filePath, lineNumber, cursor)));
                                cursor += 2;
                            } else {
                                cursor++;
                            }
                            break;
                        case '(':
                            tokens.add(new Token(TokenType.OPEN_PARENTHESIS, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case ')':
                            tokens.add(new Token(TokenType.CLOSE_PARENTHESIS, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case '{':
                            tokens.add(new Token(TokenType.OPEN_CURLY_BRACE, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case '}':
                            tokens.add(new Token(TokenType.CLOSE_CURLY_BRACE, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case '[':
                            tokens.add(new Token(TokenType.OPEN_SQUARE_BRACE, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case ']':
                            tokens.add(new Token(TokenType.CLOSE_SQUARE_BRACE, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case ',':
                            tokens.add(new Token(TokenType.COMMA, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case '"':
                            tokens.add(new Token(TokenType.STRING_LITERAL, new Location(filePath, lineNumber, cursor + 1), parseString(line)));
                            break;
                        case '\'':
                            tokens.add(new Token(TokenType.CHAR_LITERAL, new Location(filePath, lineNumber, cursor), parseChar(line)));
                            break;
                        case '.':
                            tokens.add(new Token(TokenType.DOT, new Location(filePath, lineNumber, cursor)));
                            next();
                            break;
                        case ':':
                            if (lookAhead(line) == ':') {
                                tokens.add(new Token(TokenType.COLON_COLON, new Location(filePath, lineNumber, cursor)));
                                cursor += 2;
                            } else {
                                tokens.add(new Token(TokenType.COLON, new Location(filePath, lineNumber, cursor)));
                                next();
                            }
                            break;
                        default:
                            tokens.add(new Token(TokenType.BAD_TOKEN, new Location(filePath, lineNumber, cursor)));
                            next();
                    }
                }
            } while (cursor < line.length());

            lineNumber++;
        }
        tokens.add(new Token(TokenType.EOF_TOKEN, new Location(filePath, lineNumber + 1, 0)));
        return tokens;
    }

    private char parseChar(String line) {
        matchNext(line, '\''); //skip '
        char c;
        if (charAt(line, cursor) == STRING_ESCAPE_CHAR) {
            next();
            switch (charAt(line, cursor)) {
                case 'n':
                    c = '\n';
                    break;
                case 'r':
                    c = '\r';
                    break;
                case 't':
                    c = '\t';
                    break;
                case 'f':
                    c = '\f';
                    break;
                case '"':
                    c = '\"';
                    break;
                default:
                    throw new IllegalStateException("Illegal escape character in char literal");
            }
            next();
        } else {
            c = charAt(line, cursor);
            next();
        }
        matchNext(line, '\'');
        return c;
    }

    //TODO: Multi-line Strings
    private String parseString(String line) {

        matchNext(line, '"'); //Skip opening '"'

        StringBuilder sb = new StringBuilder();
        while (cursor < line.length() && charAt(line, cursor) != '"') {
            if (charAt(line, cursor) == STRING_ESCAPE_CHAR) {
                switch (charAt(line, cursor + 1)) {
                    case 'n':
                        sb.append('\n');
                        next();
                        break;
                    case 'r':
                        sb.append('\r');
                        next();
                        break;
                    case 't':
                        sb.append('\t');
                        next();
                        break;
                    case 'f':
                        sb.append('\f');
                        next();
                        break;
                    case '"':
                        sb.append('\"');
                        next();
                        break;
                    case '\\':
                        sb.append('\\');
                        next();
                        break;
                    default:
                        throw new IllegalStateException("Illegal escape character `" + charAt(line, cursor + 1) + "`  in string literal");
                }
                next();
            } else {
                sb.append(charAt(line, cursor));
                next();
            }
        }
        matchNext(line, '"');//Skip closing '"'
        return sb.toString();
    }

    private Number parseNum(String line) {

        int start = cursor;

        next();
        while (cursor < line.length() && (Character.isDigit(charAt(line, cursor)) || (charAt(line, cursor) == '.') && !Character.isAlphabetic(charAt(line, cursor + 1)))) {
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

    private char charAt(String line, int i) {
        if (line.length() <= i) {
            return '\0';
        }
        return line.charAt(i);
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
        return charAt(line, index);
    }

    private void next() {
        cursor++;
    }

    private void matchNext(String line, char expected) {
        if (charAt(line, cursor) != expected) {
            throw new IllegalStateException("Expected `" + expected + "` but got `" + charAt(line, cursor) + "`");
        }
        next();
    }
}
