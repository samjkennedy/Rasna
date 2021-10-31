package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.diagnostics.Error;
import com.skennedy.lazuli.lexing.Lexer;
import com.skennedy.lazuli.lexing.model.Token;
import com.skennedy.lazuli.lexing.model.TokenType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.OpType;
import com.skennedy.lazuli.parsing.model.OperatorPrecedence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Parser {

    private static final Logger log = LogManager.getLogger(Parser.class);

    private int position;
    private List<Token> parsedTokens;

    private List<Error> errors;

    public Program parse(String program) {

        errors = new ArrayList<>();

        Lexer lexer = new Lexer();
        this.position = 0;
        this.parsedTokens = new ArrayList<>();
        for (Token token : lexer.lex(program)) {
            if (token.getTokenType() != TokenType.WHITESPACE && token.getTokenType() != TokenType.COMMENT) {
                parsedTokens.add(token);
            }
        }

        //System.out.println(parsedTokens.stream().map(Token::toString).collect(Collectors.joining(", ")));

        List<Expression> expressions = new ArrayList<>();
        while (current().getTokenType() != TokenType.EOF_TOKEN) {
            if (current().getTokenType() == TokenType.WHITESPACE) {
                position++;
            }
            expressions.add(parseExpression());
        }
        matchToken(TokenType.EOF_TOKEN);

        return new Program(errors, expressions);
    }

    private Expression parsePrimaryExpression() {

        switch (current().getTokenType()) {
            case INT_LITERAL:
                return matchToken(TokenType.INT_LITERAL);
            case TRUE_KEYWORD:
                return matchToken(TokenType.TRUE_KEYWORD);
            case FALSE_KEYWORD:
                return matchToken(TokenType.FALSE_KEYWORD);
            case OPEN_CURLY_BRACE:
                return parseBlockExpression();
            case OPEN_SQUARE_BRACE:
                return parseArrayLiteralExpression();
            case OPEN_PARENTHESIS:
                return parseParenthesisedExpression();
            case TYPEOF_INTR:
                return parseTypeofIntrinsic();
            case PRINT_INTR:
                return parsePrintIntrinsic();
            case IF_KEYWORD:
                return parseIfExpression();
            case WHILE_KEYWORD:
                return parseWhileExpression();
            case FOR_KEYWORD:
                return parseIteratorExpression();
            case INT_KEYWORD:
            case INT_ARRAY_KEYWORD:
            case BOOL_KEYWORD:
            case NUM_KEYWORD:
            case CONST_KEYWORD:
                return parseVariableDeclarationExpression();
            case IDENTIFIER:
                if (nextToken().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
                    return parseArrayAccessExpression();
                }
                return parseAssignmentExpression();
            default:
                throw new IllegalStateException("Unexpected value: " + current().getTokenType());
        }
    }

    private Expression parseArrayLiteralExpression() {

        IdentifierExpression openSquareBrace = matchToken(TokenType.OPEN_SQUARE_BRACE);

        //TODO: CommaSeparatedExpression
        List<Expression> elements = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_SQUARE_BRACE
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN) {

            elements.add(parseExpression());

            if (current().getTokenType() == TokenType.CLOSE_SQUARE_BRACE) {
                break;
            }
            matchToken(TokenType.COMMA);
        }
        IdentifierExpression closeSquareBrace = matchToken(TokenType.CLOSE_SQUARE_BRACE);

        return new ArrayLiteralExpression(openSquareBrace, elements, closeSquareBrace);
    }

    private Expression parseIteratorExpression() {

        IdentifierExpression forKeyword = matchToken(TokenType.FOR_KEYWORD);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);

        IdentifierExpression declarationKeyword;
        switch (current().getTokenType()) {
            case INT_KEYWORD:
                declarationKeyword = matchToken(TokenType.INT_KEYWORD);
                break;
            case BOOL_KEYWORD:
                declarationKeyword = matchToken(TokenType.BOOL_KEYWORD);
                break;
            case NUM_KEYWORD:
                declarationKeyword = matchToken(TokenType.NUM_KEYWORD);
                break;
            default:
                throw new IllegalStateException("Unexpected variable declaration keyword: " + current().getTokenType());
        }

        //TODO: Is this the best way?
        boolean isArray = false;
        if (current().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
            matchToken(TokenType.OPEN_SQUARE_BRACE);
            matchToken(TokenType.CLOSE_SQUARE_BRACE);
            isArray = true;
        }

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

        if (current().getTokenType() == TokenType.EQUALS) {
            IdentifierExpression equals = matchToken(TokenType.EQUALS);
            Expression initialiser = parseExpression();

            IdentifierExpression toKeyword = matchToken(TokenType.TO_KEYWORD);
            Expression terminator = parseExpression();

            IdentifierExpression byKeyword = null;
            Expression step = null;
            if (current().getTokenType() == TokenType.BY_KEYWORD) {
                byKeyword = matchToken(TokenType.BY_KEYWORD);
                step = parseExpression();
            }

            Expression range = null;
            if (current().getTokenType() == TokenType.IF_KEYWORD) {
                range = parseRangeExpression();
            }

            IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);
            BlockExpression body = parseBlockExpression();

            return new ForExpression(forKeyword, openParen, declarationKeyword, identifier, equals, initialiser, toKeyword, terminator, byKeyword, step, range, closeParen, body);
        } else if (current().getTokenType() == TokenType.IN_KEYWORD) {
            IdentifierExpression inKeyword = matchToken(TokenType.IN_KEYWORD);

            Expression iterable;
            if (current().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
                iterable = parseArrayLiteralExpression();
            } else {
                iterable = parseExpression();
            }
            Expression range = null;
            if (current().getTokenType() == TokenType.IF_KEYWORD) {
                matchToken(TokenType.IF_KEYWORD);
                range = parseExpression();
            }
            IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);
            BlockExpression body = parseBlockExpression();

            return new ForInExpression(forKeyword, openParen, declarationKeyword, identifier, inKeyword, iterable, range, closeParen, body);
        } else {
            throw new IllegalStateException("Unexpected token in iterator expression: " + current().getTokenType());
        }
    }

    private Expression parseRangeExpression() {
        matchToken(TokenType.IF_KEYWORD);
        return parseExpression();
    }

    private Expression parseArrayAccessExpression() {
        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression openBrace = matchToken(TokenType.OPEN_SQUARE_BRACE);
        Expression index = parseExpression();
        IdentifierExpression closeBrace = matchToken(TokenType.CLOSE_SQUARE_BRACE);

        return new ArrayAccessExpression(identifier, openBrace, index, closeBrace);
    }

    private Expression parseAssignmentExpression() {
        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

        if (current().getTokenType() != TokenType.EQUALS) {
            return identifier;
        }

        IdentifierExpression equals = matchToken(TokenType.EQUALS);
        Expression assignment = parseExpression();

        return new AssignmentExpression(identifier, equals, assignment);
    }

    //TODO: This is a declaration AND assignment, currently cannot declare a variable without an assignment
    private Expression parseVariableDeclarationExpression() {
        IdentifierExpression constKeyword = null;
        if (current().getTokenType() == TokenType.CONST_KEYWORD) {
            constKeyword = matchToken(TokenType.CONST_KEYWORD);
        }
        IdentifierExpression declarationKeyword;
        boolean isArray = false;
        switch (current().getTokenType()) {
            case INT_KEYWORD:
                declarationKeyword = matchToken(TokenType.INT_KEYWORD);
                break;
            case INT_ARRAY_KEYWORD: //TODO: Temporary until Arrays can have a type
                declarationKeyword = matchToken(TokenType.INT_ARRAY_KEYWORD);
                isArray = true;
                break;
            case BOOL_KEYWORD:
                declarationKeyword = matchToken(TokenType.BOOL_KEYWORD);
                break;
            case NUM_KEYWORD:
                declarationKeyword = matchToken(TokenType.NUM_KEYWORD);
                break;
            default:
                throw new IllegalStateException("Unexpected variable declaration keyword: " + current().getTokenType());
        }

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);


        IdentifierExpression equals = null;
        Expression initialiser = null;
        if (current().getTokenType() == TokenType.EQUALS) {
            //Variable declared but not assigned yet
            equals = matchToken(TokenType.EQUALS);
            initialiser = parseExpression();
        }

        IdentifierExpression bar = null;
        Expression range = null;
        if (current().getTokenType() == TokenType.BAR) {
            bar = matchToken(TokenType.BAR);
            range = parseExpression();
        }

        return new VariableDeclarationExpression(constKeyword, declarationKeyword, isArray, identifier, bar, range, equals, initialiser);
    }

    private Expression parseWhileExpression() {
        IdentifierExpression whileKeyword = matchToken(TokenType.WHILE_KEYWORD);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression condition = parseExpression();
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        BlockExpression body = parseBlockExpression();

        return new WhileExpression(whileKeyword, openParen, condition, closeParen, body);
    }

    private Expression parseIfExpression() {
        IdentifierExpression ifKeyword = matchToken(TokenType.IF_KEYWORD);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression condition = parseExpression();
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        Expression body = parseExpression();

        if (current().getTokenType() == TokenType.ELSE_KEYWORD) {
            IdentifierExpression elseKeyword = matchToken(TokenType.ELSE_KEYWORD);
            Expression elseBody = parseExpression();
            return new IfExpression(ifKeyword, openParen, condition, closeParen, body, elseKeyword, elseBody);
        }

        return new IfExpression(ifKeyword, openParen, condition, closeParen, body);
    }

    private Expression parseParenthesisedExpression() {
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression expression = parseExpression();
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        return new ParenthesisedExpression(openParen, expression, closeParen);
    }

    private Expression parseExpression() {

        return parseBinaryExpression(0);
    }

    //<left> <op> <right>
    private Expression parseBinaryExpression(int parentPrecedence) {

        Expression left;
        int precedence = OperatorPrecedence.getUnaryOperatorPrecedence(current());
        if (precedence != 0 && precedence >= parentPrecedence) {
            OpType operator = parseOpType();
            Expression operand = parseBinaryExpression(precedence);
            left = new UnaryExpression(operator, operand);
        } else {
            left = parsePrimaryExpression();
        }

        while (true) {
            precedence = OperatorPrecedence.getBinaryOperatorPrecedence(current());
            if (precedence == 0 || precedence <= parentPrecedence) {
                break;
            }
            OpType operator = parseOpType();
            Expression right = parseBinaryExpression(precedence);
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    // print(<EXPRESSION>)
    private Expression parsePrintIntrinsic() {

        IdentifierExpression printInstr = matchToken(TokenType.PRINT_INTR);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression expression = parseExpression();
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        return new PrintExpression(printInstr, openParen, expression, closeParen);
    }

    private Expression parseTypeofIntrinsic() {

        IdentifierExpression typeofKeyword = matchToken(TokenType.TYPEOF_INTR);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression expression = parseExpression();
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        return new TypeofExpression(typeofKeyword, openParen, expression, closeParen);
    }

    private BlockExpression parseBlockExpression() {
        matchToken(TokenType.OPEN_CURLY_BRACE);

        List<Expression> expressions = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_CURLY_BRACE) {
            expressions.add(parseExpression());
        }
        matchToken(TokenType.CLOSE_CURLY_BRACE);

        return new BlockExpression(expressions);
    }

    private OpType parseOpType() {
        Token token = current();
        position++;
        switch (token.getTokenType()) {
            case PLUS:
                return OpType.ADD;
            case MINUS:
                return OpType.SUB;
            case STAR:
                return OpType.MUL;
            case SLASH:
                return OpType.DIV;
            case PERCENT:
            case MOD_KEYWORD:
                return OpType.MOD; //TODO: Should `mod` be the modulo operator?
            case EQUALS_EQUALS:
                return OpType.EQ;
            case BANG_EQUALS:
                return OpType.NEQ;
            case GT:
                return OpType.GT;
            case LT:
                return OpType.LT;
            case GTEQ:
                return OpType.GTEQ;
            case LTEQ:
                return OpType.LTEQ;
            case OR_KEYWORD:
                return OpType.LOR;
            case AND_KEYWORD:
                return OpType.LAND;
            default:
                throw new IllegalStateException("Unexpected value: " + token.getTokenType());
        }
    }

    private IdentifierExpression matchToken(TokenType tokenType) {
        Token token = current();
        if (token.getTokenType() == tokenType) {
            position++;
            return new IdentifierExpression(token.getTokenType(), token.getValue());
        }
        errors.add(Error.raiseUnexpectedToken(tokenType, token));
        return new IdentifierExpression(TokenType.BAD_TOKEN, null);
    }

    private Token current() {
        return parsedTokens.get(position);
    }

    private Token nextToken() {
        if (position >= parsedTokens.size()) {
            return parsedTokens.get(parsedTokens.size() - 1); //EOF
        }
        return parsedTokens.get(position + 1);
    }
}
