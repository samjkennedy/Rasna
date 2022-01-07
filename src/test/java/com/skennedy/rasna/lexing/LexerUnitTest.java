package com.skennedy.rasna.lexing;

import com.skennedy.rasna.lexing.model.Token;
import com.skennedy.rasna.lexing.model.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerUnitTest {

    @Test
    void lex_givenBasicTokenStream_returnsCorrectTokens() {

        String program = "1 - 1";

        Lexer lexer = new Lexer();

        List<Token> tokens = lexer.lex("test.ras", program);

        assertEquals(6, tokens.size());

        assertEquals(TokenType.INT_LITERAL, tokens.get(0).getTokenType());
        assertEquals(TokenType.WHITESPACE, tokens.get(1).getTokenType());
        assertEquals(TokenType.MINUS, tokens.get(2).getTokenType());
        assertEquals(TokenType.WHITESPACE, tokens.get(3).getTokenType());
        assertEquals(TokenType.INT_LITERAL, tokens.get(4).getTokenType());
        assertEquals(TokenType.EOF_TOKEN, tokens.get(5).getTokenType());
    }

}