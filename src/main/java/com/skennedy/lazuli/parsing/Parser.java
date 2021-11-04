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
            case LEN_INTR:
                return parseLenIntrinsic();
            case IF_KEYWORD:
                return parseIfExpression();
            case WHILE_KEYWORD:
                return parseWhileExpression();
            case FOR_KEYWORD:
                return parseIteratorExpression();
            case VOID_KEYWORD:
            case INT_KEYWORD:
            case INT_ARRAY_KEYWORD:
            case BOOL_KEYWORD:
            case REAL_KEYWORD:
            case FUNCTION_TYPE_KEYWORD:
            case CONST_KEYWORD:
                return parseVariableOrFunctionDeclarationExpression();
            case IDENTIFIER:
                if (nextToken().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
                    return parseArrayAccessExpression();
                }
                if (nextToken().getTokenType() == TokenType.OPEN_PARENTHESIS) {
                    return parseFunctionCallExpression();
                }
                return parseAssignmentExpression();
            case RETURN_KEYWORD:
                return parseReturnExpression();
            case MATCH_KEYWORD:
                return parseMatchExpression();
            default:
                throw new IllegalStateException("Unexpected value: " + current().getTokenType());
        }
    }

    private Expression parseMatchExpression() {

        IdentifierExpression matchKeyword = matchToken(TokenType.MATCH_KEYWORD);
        Expression identifier = parseExpression();
        IdentifierExpression openCurly = matchToken(TokenType.OPEN_CURLY_BRACE);

        List<MatchCaseExpression> caseExpressions = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_CURLY_BRACE && current().getTokenType() != TokenType.ELSE_KEYWORD) {
            Expression caseExpression = parseExpression();
            IdentifierExpression arrow = matchToken(TokenType.ARROW);
            Expression thenExpression = parseExpression();
            IdentifierExpression comma = matchToken(TokenType.COMMA);

            if (caseExpression instanceof BinaryExpression && ((BinaryExpression) caseExpression).getOperation() == OpType.LOR) {

                caseExpressions.add(new MatchCaseExpression(((BinaryExpression) caseExpression).getLeft(), arrow, thenExpression, comma));
                caseExpressions.add(new MatchCaseExpression(((BinaryExpression) caseExpression).getRight(), arrow, thenExpression, comma));
            } else {
                caseExpressions.add(new MatchCaseExpression(caseExpression, arrow, thenExpression, comma));
            }
        }
        IdentifierExpression elseKeyword = matchToken(TokenType.ELSE_KEYWORD);
        IdentifierExpression arrow = matchToken(TokenType.ARROW);
        Expression thenExpression = parseExpression();

        caseExpressions.add(new MatchCaseExpression(elseKeyword, arrow, thenExpression, null));
        IdentifierExpression closeCurly = matchToken(TokenType.CLOSE_CURLY_BRACE);

        return new MatchExpression(matchKeyword, identifier, openCurly, caseExpressions, closeCurly);
    }

    private Expression parseFunctionCallExpression() {
        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);

        List<Expression> arguments = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_PARENTHESIS
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN) {

            arguments.add(parseExpression());

            if (current().getTokenType() == TokenType.CLOSE_PARENTHESIS) {
                break;
            }
            matchToken(TokenType.COMMA);
        }
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        return new FunctionCallExpression(identifier, openParen, arguments, closeParen);
    }

    private Expression parseReturnExpression() {
        IdentifierExpression returnKeyword = matchToken(TokenType.RETURN_KEYWORD);
        Expression returnValue = parseExpression();

        return new ReturnExpression(returnKeyword, returnValue);
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
            case REAL_KEYWORD:
                declarationKeyword = matchToken(TokenType.REAL_KEYWORD);
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

            Expression guard = null;
            if (current().getTokenType() == TokenType.BAR) {
                guard = parseGuardExpression();
            }

            IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);
            Expression body = parseExpression();

            return new ForExpression(forKeyword, openParen, declarationKeyword, identifier, equals, initialiser, toKeyword, terminator, byKeyword, step, guard, closeParen, body);
        } else if (current().getTokenType() == TokenType.IN_KEYWORD) {
            IdentifierExpression inKeyword = matchToken(TokenType.IN_KEYWORD);

            Expression iterable;
            if (current().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
                iterable = parseArrayLiteralExpression();
            } else {
                iterable = parseExpression();
            }
            Expression guard = null;
            if (current().getTokenType() == TokenType.BAR) {
                guard = parseGuardExpression();
            }
            IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);
            Expression body = parseExpression();

            return new ForInExpression(forKeyword, openParen, declarationKeyword, identifier, inKeyword, iterable, guard, closeParen, body);
        } else {
            throw new IllegalStateException("Unexpected token in iterator expression: " + current().getTokenType());
        }
    }

    private Expression parseGuardExpression() {
        matchToken(TokenType.BAR);
        return parseExpression();
    }

    private Expression parseArrayAccessExpression() {
        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression openBrace = matchToken(TokenType.OPEN_SQUARE_BRACE);
        Expression index = parseExpression();
        IdentifierExpression closeBrace = matchToken(TokenType.CLOSE_SQUARE_BRACE);

        ArrayAccessExpression arrayAccessExpression = new ArrayAccessExpression(identifier, openBrace, index, closeBrace);
        if (current().getTokenType() != TokenType.EQUALS) {
            return arrayAccessExpression;
        }
        IdentifierExpression equals = matchToken(TokenType.EQUALS);
        Expression assignment = parseExpression();

        return new ArrayAssignmentExpression(arrayAccessExpression, equals, assignment);
    }

    private Expression parseAssignmentExpression() {
        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

        if (current().getTokenType() == TokenType.INCREMENT || current().getTokenType() == TokenType.DECREMENT) {
            IdentifierExpression operator;
            switch (current().getTokenType()) {
                case DECREMENT:
                    operator = matchToken(TokenType.DECREMENT);
                    break;
                case INCREMENT:
                    operator = matchToken(TokenType.INCREMENT);
                    break;
                default:
                    throw new IllegalStateException("How did you get here?");
            }
            return new IncrementExpression(identifier, operator);
        }

        if (current().getTokenType() != TokenType.EQUALS) {
            return identifier;
        }

        IdentifierExpression equals = matchToken(TokenType.EQUALS);
        Expression assignment = parseExpression();

        return new AssignmentExpression(identifier, equals, assignment);
    }

    //TODO: This is a declaration AND assignment, currently cannot declare a variable without an assignment
    private Expression parseVariableOrFunctionDeclarationExpression() {
        IdentifierExpression constKeyword = null;
        if (current().getTokenType() == TokenType.CONST_KEYWORD) {
            constKeyword = matchToken(TokenType.CONST_KEYWORD);
        }
        IdentifierExpression typeKeyword;
        boolean isArray = false;
        switch (current().getTokenType()) {
            case VOID_KEYWORD:
                typeKeyword = matchToken(TokenType.VOID_KEYWORD);
                break;
            case INT_KEYWORD:
                typeKeyword = matchToken(TokenType.INT_KEYWORD);
                break;
            case INT_ARRAY_KEYWORD: //TODO: Temporary until Arrays can have a type
                typeKeyword = matchToken(TokenType.INT_ARRAY_KEYWORD);
                isArray = true;
                break;
            case BOOL_KEYWORD:
                typeKeyword = matchToken(TokenType.BOOL_KEYWORD);
                break;
            case REAL_KEYWORD:
                typeKeyword = matchToken(TokenType.REAL_KEYWORD);
                break;
            case FUNCTION_TYPE_KEYWORD:
                typeKeyword = matchToken(TokenType.FUNCTION_TYPE_KEYWORD);
                break;
            default:
                throw new IllegalStateException("Unexpected variable declaration keyword: " + current().getTokenType());
        }

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

        if (current().getTokenType() == TokenType.OPEN_PARENTHESIS) {
            IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);

            List<FunctionArgumentExpression> argumentExpressions = new ArrayList<>();
            while (current().getTokenType() != TokenType.CLOSE_PARENTHESIS) {
                argumentExpressions.add(parseFunctionArgumentExpression());

                if (current().getTokenType() == TokenType.COMMA) {
                    matchToken(TokenType.COMMA);
                }
            }
            IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

            BlockExpression body = parseBlockExpression();

            return new FunctionDeclarationExpression(typeKeyword, identifier, openParen, argumentExpressions, closeParen, body);
        }

        if (typeKeyword.getTokenType() == TokenType.VOID_KEYWORD) {
            throw new IllegalStateException("Variables cannot be of type Void");
        }

        IdentifierExpression equals = null;
        Expression initialiser = null;
        if (current().getTokenType() == TokenType.EQUALS) {
            //Variable declared but not assigned yet
            equals = matchToken(TokenType.EQUALS);
            initialiser = parseExpression();
        }

        IdentifierExpression bar = null;
        Expression guard = null;
        if (current().getTokenType() == TokenType.BAR) {
            guard = parseGuardExpression();
        }

        return new VariableDeclarationExpression(constKeyword, typeKeyword, isArray, identifier, bar, guard, equals, initialiser);
    }

    private FunctionArgumentExpression parseFunctionArgumentExpression() {
        IdentifierExpression constKeyword = null;
        if (current().getTokenType() == TokenType.CONST_KEYWORD) {
            constKeyword = matchToken(TokenType.CONST_KEYWORD);
        }
        IdentifierExpression typeKeyword;
        switch (current().getTokenType()) {
            case INT_KEYWORD:
                typeKeyword = matchToken(TokenType.INT_KEYWORD);
                break;
            case INT_ARRAY_KEYWORD: //TODO: Temporary until Arrays can have a type
                typeKeyword = matchToken(TokenType.INT_ARRAY_KEYWORD);
                break;
            case BOOL_KEYWORD:
                typeKeyword = matchToken(TokenType.BOOL_KEYWORD);
                break;
            case REAL_KEYWORD:
                typeKeyword = matchToken(TokenType.REAL_KEYWORD);
                break;
            case FUNCTION_TYPE_KEYWORD:
                typeKeyword = matchToken(TokenType.FUNCTION_TYPE_KEYWORD);
                break;
            default:
                throw new IllegalStateException("Unexpected variable declaration keyword: " + current().getTokenType());
        }
        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

        IdentifierExpression bar = null;
        Expression guard = null;
        if (current().getTokenType() == TokenType.BAR) {
            guard = parseGuardExpression();
        }

        return new FunctionArgumentExpression(constKeyword, typeKeyword, identifier, bar, guard);
    }

    private Expression parseWhileExpression() {
        IdentifierExpression whileKeyword = matchToken(TokenType.WHILE_KEYWORD);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression condition = parseExpression();
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        Expression body = parseExpression();

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


    private Expression parseLenIntrinsic() {
        IdentifierExpression len = matchToken(TokenType.LEN_INTR);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression expression = parseExpression();
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        return new ArrayLengthExpression(len, openParen, expression, closeParen);
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
