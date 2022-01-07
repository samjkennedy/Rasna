package com.skennedy.rasna.parsing;

import com.skennedy.rasna.Rasna;
import com.skennedy.rasna.diagnostics.Error;
import com.skennedy.rasna.lexing.Lexer;
import com.skennedy.rasna.lexing.model.Location;
import com.skennedy.rasna.lexing.model.Token;
import com.skennedy.rasna.lexing.model.TokenType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.OpType;
import com.skennedy.rasna.parsing.model.OperatorPrecedence;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Parser {

    private static final Logger log = LogManager.getLogger(Parser.class);

    private int position;
    private List<Token> tokensToParse;

    private List<Error> errors;

    private Path filePath;

    public Program parse(Path filePath, String program) {
        this.filePath = filePath;

        errors = new ArrayList<>();
        List<Expression> expressions = new ArrayList<>();

        Lexer lexer = new Lexer();
        this.position = 0;
        this.tokensToParse = new ArrayList<>();
        for (Token token : lexer.lex(filePath.getFileName().toString(), program)) {
            if (token.getTokenType() != TokenType.WHITESPACE && token.getTokenType() != TokenType.COMMENT) {
                tokensToParse.add(token);
            }
        }

        //System.out.println(tokensToParse.stream().map(Token::toString).collect(Collectors.joining(", ")));

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
            case CONST_KEYWORD:
                return parseVariableDeclarationExpression();
            case INT_LITERAL:
                return matchToken(TokenType.INT_LITERAL);
            case STRING_LITERAL:
                return matchToken(TokenType.STRING_LITERAL);
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
            case MAP_INTR:
                return parseMapIntrinsic();
            case IF_KEYWORD:
                return parseIfExpression();
            case WHILE_KEYWORD:
                return parseWhileExpression();
            case FOR_KEYWORD:
                return parseIteratorExpression();
            case STRUCT_KEYWORD:
                return parseStructDeclarationExpression();
            case IDENTIFIER:
                if (nextToken().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
                    return parseArrayAccessExpression();
                }
                if (nextToken().getTokenType() == TokenType.OPEN_PARENTHESIS) {
                    return parseFunctionCallExpression();
                }
                if (nextToken().getTokenType() == TokenType.IDENTIFIER) {
                    if (lookAhead(2).getTokenType() == TokenType.COLON) {
                        return parseVariableDeclarationExpression();
                    }
                }
                if (nextToken().getTokenType() == TokenType.DOT) {
                    return parseMemberAccessorExpression();
                }
                if (nextToken().getTokenType() == TokenType.COLON) {
                    return parseVariableDeclarationExpression();
                }
                if (nextToken().getTokenType() == TokenType.COLON_COLON) {
                    return parseNamespaceAccessorExpression();
                }
                return parseAssignmentExpression();
            case RETURN_KEYWORD:
                return parseReturnExpression();
            case MATCH_KEYWORD:
                return parseMatchExpression();
            case YIELD_KEYWORD:
                return parseYieldExpression();
            case FN_KEYWORD:
                return parseFunctionDeclarationExpression();
            case IMPORT_KEYWORD:
                return parseImportStatement();
            case NAMESPACE_KEYWORD:
                return parseNamespaceExpression();
            default:
                errors.add(Error.raiseUnexpectedToken(current()));
                matchToken(current().getTokenType());
                return new BlockExpression(Collections.emptyList());
        }
    }

    private Expression parseNamespaceAccessorExpression() {

        IdentifierExpression namespace = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression namespaceAccessor = matchToken(TokenType.COLON_COLON);
        Expression expression = parseExpression();

        return new NamespaceAccessorExpression(namespace, namespaceAccessor, expression);
    }

    /**
     * Parses a block of code within a given namespace
     *
     * @return A namespace expression
     */
    //Kind of dubious as to whether this is truly an `expression`
    private Expression parseNamespaceExpression() {

        IdentifierExpression namespaceKeyword = matchToken(TokenType.NAMESPACE_KEYWORD);
        IdentifierExpression namespace = matchToken(TokenType.IDENTIFIER);
        BlockExpression blockExpression = parseBlockExpression();

        return new NamespaceExpression(namespaceKeyword, namespace, blockExpression, false);
    }

    //TODO: This should all be done as part of the binder to allow better error reporting
    private Expression parseImportStatement() {
        matchToken(TokenType.IMPORT_KEYWORD);

        boolean inline = false;
        if (current().getTokenType() == TokenType.INLINE_KEYWORD) {
            matchToken(TokenType.INLINE_KEYWORD);
            inline = true;
        }
        IdentifierExpression importPath = matchToken(TokenType.STRING_LITERAL);

        Path path = filePath.getParent().resolve((String) importPath.getValue());

        String fileNameWithExt = path.getFileName().toString();
        String[] fileParts = fileNameWithExt.split("\\.");
        String fileName = fileParts[0];
        String fileExt = fileParts[1];

        if (current().getTokenType() == TokenType.AS_KEYWORD) {
            if (inline) {
                //TODO: Make this a compiler warning not a java warning
                log.warn("No need to rename inline imports");
            }
            matchToken(TokenType.AS_KEYWORD);
            IdentifierExpression name = matchToken(TokenType.IDENTIFIER);

            fileName = (String) name.getValue();
        }

        if (!Rasna.FILE_EXT.equals(fileExt)) {
            throw new IllegalArgumentException("File must be a ." + Rasna.FILE_EXT + " file.");
        }

        try {
            String code = String.join(StringUtils.LF, Files.readAllLines(path));

            Parser parser = new Parser();
            Program program = parser.parse(path.toAbsolutePath(), code);

            if (program.hasErrors()) {
                for (Error error : program.getErrors()) {
                    System.err.println(error.getMessage() + " at " + error.getLocation() + " -> " + error.getToken());
                }
                System.exit(1);
            }

            //This is real scuffed
            return new NamespaceExpression(
                    new IdentifierExpression(new Token(TokenType.NAMESPACE_KEYWORD, new Location(fileNameWithExt, -1, -1)), TokenType.NAMESPACE_KEYWORD, TokenType.NAMESPACE_KEYWORD.getText()),
                    new IdentifierExpression(new Token(TokenType.IDENTIFIER, new Location(fileNameWithExt, -1, -1), fileName), TokenType.IDENTIFIER, fileName),
                    new BlockExpression(program.getExpressions()),
                    inline
            );

        } catch (IOException e) {
            errors.add(Error.raiseImportError(path, importPath.getToken()));
        }
        return new BlockExpression(Collections.emptyList());
    }

    private Expression parseMemberAccessorExpression() {

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression dot = matchToken(TokenType.DOT);

        if (current().getTokenType() == TokenType.OPEN_PARENTHESIS) {
            throw new UnsupportedOperationException("Member method calls are not yet supported");
        }

        IdentifierExpression member = matchToken(TokenType.IDENTIFIER);

        MemberAccessorExpression memberAccessorExpression = new MemberAccessorExpression(identifier, dot, member);

        if (current().getTokenType() == TokenType.EQUALS) {
            IdentifierExpression equals = matchToken(TokenType.EQUALS);
            Expression assignment = parseExpression();

            return new MemberAssignmentExpression(memberAccessorExpression, equals, assignment);
        }

        //TODO: This might be more than just an identifier, e.g. method call: getVal().x
        //      Eventually ditch the need for the identifier and use whatever was evaluated last
        return memberAccessorExpression;
    }

    private Expression parseYieldExpression() {
        IdentifierExpression yieldKeyword = matchToken(TokenType.YIELD_KEYWORD);
        Expression expression = parseExpression();

        return new YieldExpression(yieldKeyword, expression);
    }

    private Expression parseMatchExpression() {

        IdentifierExpression matchKeyword = matchToken(TokenType.MATCH_KEYWORD);
        Expression identifier = parseExpression();
        IdentifierExpression openCurly = matchToken(TokenType.OPEN_CURLY_BRACE);

        List<MatchCaseExpression> caseExpressions = new ArrayList<>();
        while (
                current().getTokenType() != TokenType.CLOSE_CURLY_BRACE
                        && current().getTokenType() != TokenType.ELSE_KEYWORD) {

            Expression caseExpression = parseExpression();

            if (current().getTokenType() == TokenType.TO_KEYWORD) {
                IdentifierExpression toKeyword = matchToken(TokenType.TO_KEYWORD);
                Expression terminator = parseExpression();

                IdentifierExpression byKeyword = null;
                Expression step = null;
                if (current().getTokenType() == TokenType.BY_KEYWORD) {
                    byKeyword = matchToken(TokenType.BY_KEYWORD);
                    step = parseExpression();
                }
                caseExpression = new RangeExpression(caseExpression, toKeyword, terminator, byKeyword, step);
            }

            IdentifierExpression arrow = matchToken(TokenType.ARROW);
            Expression thenExpression = parseExpression();

            IdentifierExpression comma = matchToken(TokenType.COMMA);

            //TODO: This is a concern of the rewriter, not the parser. A case could be infinitely nested ors
            if (caseExpression instanceof BinaryExpression) {
                if (((BinaryExpression) caseExpression).getOperation() == OpType.LOR) {

                    caseExpressions.add(new MatchCaseExpression(((BinaryExpression) caseExpression).getLeft(), arrow, thenExpression, comma));
                    caseExpressions.add(new MatchCaseExpression(((BinaryExpression) caseExpression).getRight(), arrow, thenExpression, comma));
                } else {
                    caseExpressions.add(new MatchCaseExpression(caseExpression, arrow, thenExpression, comma));
                }
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

            if (current().getTokenType() == TokenType.OPEN_CURLY_BRACE) {
                StructLiteralExpression structLiteralExpression = parseStructLiteralExpression(null);
                arguments.add(structLiteralExpression);
            } else {
                arguments.add(parseExpression());
            }

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

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

        TypeExpression declarationKeyword = parseTypeExpression();

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
            RangeExpression rangeExpression = new RangeExpression(initialiser, toKeyword, terminator, byKeyword, step);

            Expression guard = null;
            if (current().getTokenType() == TokenType.BAR) {
                guard = parseGuardExpression();
            }

            IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);
            Expression body = parseBlockExpression();

            return new ForExpression(forKeyword, openParen, declarationKeyword, identifier, equals, rangeExpression, guard, closeParen, body);

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

    private Expression parseStructDeclarationExpression() {
        IdentifierExpression structKeyword = matchToken(TokenType.STRUCT_KEYWORD);
        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression openCurly = matchToken(TokenType.OPEN_CURLY_BRACE);

        List<Expression> members = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_CURLY_BRACE
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN
        ) {
            members.add(parseExpression());
        }
        IdentifierExpression closeCurly = matchToken(TokenType.CLOSE_CURLY_BRACE);

        return new StructDeclarationExpression(structKeyword, identifier, openCurly, members, closeCurly);
    }

    private Expression parseFunctionDeclarationExpression() {

        IdentifierExpression fnKeyword = matchToken(TokenType.FN_KEYWORD);

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);

        List<FunctionArgumentExpression> argumentExpressions = new ArrayList<>();
        while (current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN
                && current().getTokenType() != TokenType.CLOSE_PARENTHESIS) {
            argumentExpressions.add(parseFunctionArgumentExpression());

            if (current().getTokenType() == TokenType.COMMA) {
                matchToken(TokenType.COMMA);
            }
        }
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        TypeExpression typeExpression = null;
        if (current().getTokenType() == TokenType.COLON) {
            typeExpression = parseTypeExpression();
        }

        BlockExpression body = parseBlockExpression();

        return new FunctionDeclarationExpression(fnKeyword, identifier, openParen, argumentExpressions, closeParen, typeExpression, body);
    }

    private VariableDeclarationExpression parseVariableDeclarationExpression() {
        IdentifierExpression constKeyword = null;
        if (current().getTokenType() == TokenType.CONST_KEYWORD) {
            constKeyword = matchToken(TokenType.CONST_KEYWORD);
        }

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

        TypeExpression typeExpression = parseTypeExpression();

        IdentifierExpression equals = null;
        Expression initialiser = null;
        if (current().getTokenType() == TokenType.EQUALS) {
            //Variable declared but not assigned yet
            equals = matchToken(TokenType.EQUALS);

            if (typeExpression.getIdentifier().getTokenType() == TokenType.FUNCTION_KEYWORD) {
                initialiser = parseLambdaExpression();
            } else if (TokenType.typeTokens.contains(current().getTokenType())) {
                initialiser = parseArrayDeclarationExpression();
            } else if (current().getTokenType() == TokenType.OPEN_CURLY_BRACE) {
                initialiser = parseStructLiteralExpression(typeExpression);
            } else {
                initialiser = parseExpression();
            }
        }

        IdentifierExpression bar = null;
        Expression guard = null;
        if (current().getTokenType() == TokenType.BAR) {
            guard = parseGuardExpression();
        }

        return new VariableDeclarationExpression(constKeyword, typeExpression, identifier, bar, guard, equals, initialiser);
    }

    private Expression parseArrayDeclarationExpression() {
        IdentifierExpression colon = matchToken(TokenType.COLON);
        IdentifierExpression typeKeyword;
        switch (current().getTokenType()) {
            case INT_KEYWORD:
                typeKeyword = matchToken(TokenType.INT_KEYWORD);
                break;
            case BOOL_KEYWORD:
                typeKeyword = matchToken(TokenType.BOOL_KEYWORD);
                break;
            case REAL_KEYWORD:
                typeKeyword = matchToken(TokenType.REAL_KEYWORD);
                break;
            case STRING_KEYWORD:
                typeKeyword = matchToken(TokenType.STRING_KEYWORD);
                break;
            case TUPLE_KEYWORD:
                typeKeyword = matchToken(TokenType.TUPLE_KEYWORD);
                break;
            case FUNCTION_KEYWORD:
                typeKeyword = matchToken(TokenType.FUNCTION_KEYWORD);
                break;
            case ANY_KEYWORD:
                typeKeyword = matchToken(TokenType.ANY_KEYWORD);
                break;
            default:
                throw new IllegalStateException("Unexpected variable declaration keyword: " + current().getTokenType());
        }
        IdentifierExpression openSquareBrace = matchToken(TokenType.OPEN_SQUARE_BRACE);
        Expression elementCount = parseExpression();
        IdentifierExpression closeSquareBrace = matchToken(TokenType.CLOSE_SQUARE_BRACE);

        return new ArrayDeclarationExpression(new TypeExpression(colon, typeKeyword, null, null), openSquareBrace, elementCount, closeSquareBrace);
    }

    private Expression parseLambdaExpression() {

        List<FunctionArgumentExpression> functionArgumentExpressions = new ArrayList<>();
        //Multi-variable lambda, TODO:
        if (current().getTokenType() == TokenType.OPEN_PARENTHESIS) {
            IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
            while (current().getTokenType() != TokenType.CLOSE_PARENTHESIS
                    && current().getTokenType() != TokenType.EOF_TOKEN
                    && current().getTokenType() != TokenType.BAD_TOKEN) {
                functionArgumentExpressions.add(parseFunctionArgumentExpression());

                if (current().getTokenType() == TokenType.COMMA) {
                    matchToken(TokenType.COMMA);
                }
            }
            IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);
        } else {

            TypeExpression typeExpression = parseTypeExpression();
            IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

            //TODO: Lambda guards
            if (current().getTokenType() == TokenType.BAR) {
                throw new UnsupportedOperationException("Lambda variable guards are not yet supported");
            }
            functionArgumentExpressions.add(new FunctionArgumentExpression(null, typeExpression, identifier, null, null));
        }
        IdentifierExpression arrow = matchToken(TokenType.ARROW);
        Expression expression = parseExpression();

        return new LambdaExpression(functionArgumentExpressions, arrow, expression);
    }

    private Expression parseParenthesisedExpression() {
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression expression = parseExpression();
        if (current().getTokenType() == TokenType.COMMA) {
            List<Expression> elements = new ArrayList<>();
            elements.add(expression);
            matchToken(TokenType.COMMA);
            while (current().getTokenType() != TokenType.CLOSE_PARENTHESIS
                    && current().getTokenType() != TokenType.EOF_TOKEN
                    && current().getTokenType() != TokenType.BAD_TOKEN) {

                elements.add(parseExpression());

                if (current().getTokenType() == TokenType.CLOSE_PARENTHESIS) {
                    break;
                }
                matchToken(TokenType.COMMA);
            }
            IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

            return new TupleLiteralExpression(openParen, elements, closeParen);
        }

        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        return new ParenthesisedExpression(openParen, expression, closeParen);
    }

    private TypeExpression parseTypeExpression() {

        IdentifierExpression colon = matchToken(TokenType.COLON);

        IdentifierExpression typeKeyword;

        Expression namespaceExpression = null;
        if (current().getTokenType() == TokenType.IDENTIFIER && nextToken().getTokenType() == TokenType.COLON_COLON) {
            namespaceExpression = parseNamespaceAccessorExpression();
        }
        typeKeyword = parseTypeKeyword();
        IdentifierExpression openSquareBrace = null;
        IdentifierExpression closeSquareBrace = null;
        if (current().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
            openSquareBrace = matchToken(TokenType.OPEN_SQUARE_BRACE);
            closeSquareBrace = matchToken(TokenType.CLOSE_SQUARE_BRACE);
        }
        return new TypeExpression(colon, typeKeyword, openSquareBrace, closeSquareBrace);
    }

    private IdentifierExpression parseTypeKeyword() {
        IdentifierExpression typeKeyword;
        switch (current().getTokenType()) {
            case INT_KEYWORD:
                typeKeyword = matchToken(TokenType.INT_KEYWORD);
                break;
            case BOOL_KEYWORD:
                typeKeyword = matchToken(TokenType.BOOL_KEYWORD);
                break;
            case REAL_KEYWORD:
                typeKeyword = matchToken(TokenType.REAL_KEYWORD);
                break;
            case STRING_KEYWORD:
                typeKeyword = matchToken(TokenType.STRING_KEYWORD);
                break;
            case TUPLE_KEYWORD:
                typeKeyword = matchToken(TokenType.TUPLE_KEYWORD);
                break;
            case FUNCTION_KEYWORD:
                typeKeyword = matchToken(TokenType.FUNCTION_KEYWORD);
                break;
            case ANY_KEYWORD:
                typeKeyword = matchToken(TokenType.ANY_KEYWORD);
                break;
            default:
                typeKeyword = matchToken(TokenType.IDENTIFIER);
                break;
        }
        return typeKeyword;
    }

    private FunctionArgumentExpression parseFunctionArgumentExpression() {

        VariableDeclarationExpression variableDeclarationExpression = parseVariableDeclarationExpression();

        if (variableDeclarationExpression.getInitialiser() != null) {
            errors.add(Error.raise("Default values for function arguments are not yet supported", variableDeclarationExpression.getEquals().getToken()));
        }

        return new FunctionArgumentExpression(
                variableDeclarationExpression.getConstKeyword(),
                variableDeclarationExpression.getTypeExpression(),
                variableDeclarationExpression.getIdentifier(),
                variableDeclarationExpression.getBar(),
                variableDeclarationExpression.getGuard()
        );
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

    private Expression parseExpression() {

        return parseAhead(parseBinaryExpression(0));
    }

    /**
     * Looks ahead to see if there's more to the expression on the right, e.g. method accessor, cast, namespace accessor etc
     *
     * @param parsed what has already been parsed to the left
     * @return
     */
    private Expression parseAhead(Expression parsed) {

        switch (current().getTokenType()) {
            case COLON_COLON:

                IdentifierExpression namespaceAccessor = matchToken(TokenType.COLON_COLON);
                Expression expression = parseExpression();

                throw new UnsupportedOperationException("Nested namespace accessors are not yet supported");
                //return parseAhead(new NamespaceAccessorExpression(parsed, namespaceAccessor, expression));
            case DOT:
                IdentifierExpression dot = matchToken(TokenType.DOT);

                if (current().getTokenType() == TokenType.OPEN_PARENTHESIS) {
                    throw new UnsupportedOperationException("Member method calls are not yet supported");
                }

                IdentifierExpression member = matchToken(TokenType.IDENTIFIER);

                return parseAhead(new MemberAccessorExpression(parsed, dot, member));
            case AS_KEYWORD:
                IdentifierExpression asKeyword = matchToken(TokenType.AS_KEYWORD);

                IdentifierExpression typeKeyword;

                Expression namespaceExpression = null;
                if (current().getTokenType() == TokenType.IDENTIFIER && nextToken().getTokenType() == TokenType.COLON_COLON) {
                    namespaceExpression = parseNamespaceAccessorExpression();
                }
                typeKeyword = parseTypeKeyword();
                IdentifierExpression openSquareBrace = null;
                IdentifierExpression closeSquareBrace = null;
                if (current().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
                    openSquareBrace = matchToken(TokenType.OPEN_SQUARE_BRACE);
                    closeSquareBrace = matchToken(TokenType.CLOSE_SQUARE_BRACE);
                }
                TypeExpression typeExpression = new TypeExpression(asKeyword, typeKeyword, openSquareBrace, closeSquareBrace);

                return parseAhead(new CastExpression(parsed, typeExpression));
//            case EQUALS:
//                if (parsed instanceof MemberAccessorExpression) {
//                    IdentifierExpression equals = matchToken(TokenType.EQUALS);
//                    Expression initialiser = parseExpression();
//
//                    return new AssignmentExpression(parsed, equals, initialiser);
//                }
//                throw new UnsupportedOperationException("Assignment to expressions is not supported");
            case IS_KEYWORD:
                IdentifierExpression isKeyword = matchToken(TokenType.IS_KEYWORD);
                IdentifierExpression typeIdentifier = matchToken(current().getTokenType());

                return parseAhead(new TypeTestExpression(parsed, isKeyword, typeIdentifier));
            default:
                return parsed;
        }

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
            Token current = current();
            precedence = OperatorPrecedence.getBinaryOperatorPrecedence(current);
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

//    private Expression parseReduceIntrinsic() {
//        IdentifierExpression reduce = matchToken(TokenType.REDUCE_INTR);
//        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
//        Expression expression = parseExpression();
//        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);
//
//        return new ReduceExpression(reduce, openParen, expression, closeParen);
//    }

    private Expression parseMapIntrinsic() {
        IdentifierExpression map = matchToken(TokenType.MAP_INTR);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression lambda = parseLambdaExpression();
        IdentifierExpression comma = matchToken(TokenType.COMMA);
        Expression mapFunction = parseExpression();
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        return new MapExpression(map, openParen, lambda, comma, mapFunction, closeParen);
    }

    private Expression parseTypeofIntrinsic() {

        IdentifierExpression typeofKeyword = matchToken(TokenType.TYPEOF_INTR);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression expression = parseExpression();
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        return new TypeofExpression(typeofKeyword, openParen, expression, closeParen);
    }

    private StructLiteralExpression parseStructLiteralExpression(TypeExpression typeExpression) {
        IdentifierExpression openCurly = matchToken(TokenType.OPEN_CURLY_BRACE);

        List<Expression> expressions = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_CURLY_BRACE
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN) {
            expressions.add(parseExpression());

            if (current().getTokenType() == TokenType.COMMA) {
                matchToken(TokenType.COMMA);
            }
        }
        IdentifierExpression closeCurly = matchToken(TokenType.CLOSE_CURLY_BRACE);

        return new StructLiteralExpression(typeExpression, openCurly, expressions, closeCurly);
    }

    private BlockExpression parseBlockExpression() {
        matchToken(TokenType.OPEN_CURLY_BRACE);

        List<Expression> expressions = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_CURLY_BRACE
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN) {
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
            case CLOSE_ANGLE_BRACE:
                return OpType.GT;
            case OPEN_ANGLE_BRACE:
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
            return new IdentifierExpression(token, token.getTokenType(), token.getValue());
        }
        errors.add(Error.raiseUnexpectedToken(tokenType, token));
        return new IdentifierExpression(token, TokenType.BAD_TOKEN, null);
    }

    private Token current() {
        if (position >= tokensToParse.size()) {
            return new Token(TokenType.EOF_TOKEN, null);
        }
        return tokensToParse.get(position);
    }

    private Token nextToken() {
        return lookAhead(1);
    }

    private Token lookAhead(int offset) {
        if (position + offset >= tokensToParse.size()) {
            return tokensToParse.get(tokensToParse.size() - 1); //EOF
        }
        return tokensToParse.get(position + offset);
    }
}
