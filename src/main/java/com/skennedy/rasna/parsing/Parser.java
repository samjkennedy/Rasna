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
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Parser {

    private static final Logger log = LogManager.getLogger(Parser.class);

    private int position;
    private List<Token> tokensToParse;

    private List<Error> errors;

    private Path filePath;

    private static List<TokenType> allowedTopLevelTokens = Arrays.asList(
            TokenType.FN_KEYWORD,
            TokenType.STRUCT_KEYWORD,
            TokenType.IMPORT_KEYWORD,
            TokenType.NAMESPACE_KEYWORD,
            TokenType.ENUM_KEYWORD,
            TokenType.INTERFACE_KEYWORD
    );
    private boolean inTopLevel = true;

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

        if (inTopLevel && !allowedTopLevelTokens.contains(current().getTokenType())) {
            errors.add(Error.raiseUnexpectedTokenAtTopLevel(current().getTokenType(), current()));
        }
        switch (current().getTokenType()) {
            case CONST_KEYWORD:
                return parseVariableDeclarationExpression();
            case NUM_LITERAL:
                if (nextToken().getTokenType() == TokenType.DOT) {
                    return parseMemberAccessorExpression(matchToken(TokenType.NUM_LITERAL));
                }
                return matchToken(TokenType.NUM_LITERAL);
            case CHAR_LITERAL:
                if (nextToken().getTokenType() == TokenType.DOT) {
                    return parseMemberAccessorExpression(matchToken(TokenType.CHAR_LITERAL));
                }
                return matchToken(TokenType.CHAR_LITERAL);
            case STRING_LITERAL:
                if (nextToken().getTokenType() == TokenType.DOT) {
                    return parseMemberAccessorExpression(matchToken(TokenType.STRING_LITERAL));
                }
                return matchToken(TokenType.STRING_LITERAL);
            case TRUE_KEYWORD:
                if (nextToken().getTokenType() == TokenType.DOT) {
                    return parseMemberAccessorExpression(matchToken(TokenType.TRUE_KEYWORD));
                }
                return matchToken(TokenType.TRUE_KEYWORD);
            case FALSE_KEYWORD:
                if (nextToken().getTokenType() == TokenType.DOT) {
                    return parseMemberAccessorExpression(matchToken(TokenType.FALSE_KEYWORD));
                }
                return matchToken(TokenType.FALSE_KEYWORD);
            case NOT_KEYWORD:
                return parseUnaryExpression();
            case OPEN_CURLY_BRACE:
                return parseBlockExpression();
            case OPEN_SQUARE_BRACE:
                return parseArrayLiteralExpression();
            case OPEN_PARENTHESIS:
                if (nextToken().getTokenType() == TokenType.CLOSE_PARENTHESIS) {
                    IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
                    matchToken(TokenType.CLOSE_PARENTHESIS);
                    return new IdentifierExpression(new Token(TokenType.UNIT_LITERAL, Location.fromOffset(openParen.getSpan().getStart(), 0), null), TokenType.UNIT_LITERAL, null);
                }
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
            case STRUCT_KEYWORD:
                return parseStructDeclarationExpression();
            case IDENTIFIER:
                if (nextToken().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
                    return parseArrayAccessExpression();
                }
                if (nextToken().getTokenType() == TokenType.OPEN_PARENTHESIS) {
                    return parseFunctionCallExpression();
                }

                //This is a bit spicy
                if (nextToken().getTokenType() == TokenType.IDENTIFIER
                        && lookAhead(2).getTokenType() == TokenType.COLON
                        && lookAhead(3).getTokenType() == TokenType.IDENTIFIER) {
                    return parseVariableDeclarationExpression();
                }
                if (nextToken().getTokenType() == TokenType.DOT || nextToken().getTokenType() == TokenType.ARROW) {
                    return parseMemberAccessorExpression(matchToken(TokenType.IDENTIFIER));
                }
                if (nextToken().getTokenType() == TokenType.COLON) {
                    return parseVariableDeclarationExpression();
                }
                if (nextToken().getTokenType() == TokenType.COLON_COLON) {
                    return parseNamespaceAccessorExpression();
                }
                if (nextToken().getTokenType() == TokenType.OPEN_CURLY_BRACE) {
                    IdentifierExpression typeKeyword = parseTypeKeyword();
                    TypeExpression typeExpression = new TypeExpression(typeKeyword);
                    return parseStructLiteralExpression(typeExpression);
                }
                return parseAssignmentExpression();
            case RETURN_KEYWORD:
                return parseReturnExpression();
            case MATCH_KEYWORD:
                return parseMatchExpression();
            case FN_KEYWORD:
                return parseFunctionDeclarationExpression();
            case IMPORT_KEYWORD:
                return parseImportStatement();
            case NAMESPACE_KEYWORD:
                return parseNamespaceExpression();
            case ENUM_KEYWORD:
                return parseEnumDeclaration();
            case INTERFACE_KEYWORD:
                return parseInterface();
            case EOF_TOKEN:
            default:
                errors.add(Error.raiseUnexpectedToken(current()));
                matchToken(current().getTokenType());
                return new NoOpExpression();
        }
    }

    private Expression parseInterface() {
        IdentifierExpression interfaceKeyword = matchToken(TokenType.INTERFACE_KEYWORD);
        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression openCurly = matchToken(TokenType.OPEN_CURLY_BRACE);

        List<FunctionSignatureExpression> signatureExpressions = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_CURLY_BRACE
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN) {
            FunctionSignatureExpression signatureExpression = parseFunctionSignature();
            signatureExpressions.add(signatureExpression);
        }
        IdentifierExpression closeCurly = matchToken(TokenType.CLOSE_CURLY_BRACE);

        return new InterfaceExpression(interfaceKeyword, identifier, openCurly, signatureExpressions, closeCurly);
    }

    private FunctionSignatureExpression parseFunctionSignature() {
        List<Expression> genericParameters = new ArrayList<>();
        if (current().getTokenType() == TokenType.OPEN_ANGLE_BRACE) {
            IdentifierExpression openAngle = matchToken(TokenType.OPEN_ANGLE_BRACE);

            genericParameters.add(parseTypeExpression());
            while (current().getTokenType() != TokenType.CLOSE_ANGLE_BRACE
                    && current().getTokenType() != TokenType.EOF_TOKEN
                    && current().getTokenType() != TokenType.BAD_TOKEN) {
                matchToken(TokenType.COMMA);
                genericParameters.add(parseTypeExpression());
            }
            IdentifierExpression closeAngle = matchToken(TokenType.CLOSE_ANGLE_BRACE);
            throw new UnsupportedOperationException("Generic parameters in interfaces are not yet implemented");
        }

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);

        List<FunctionParameterExpression> argumentExpressions = new ArrayList<>();
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
            matchToken(TokenType.COLON);
            typeExpression = parseTypeExpression();
        }

        return new FunctionSignatureExpression(identifier, openParen, argumentExpressions, closeParen, typeExpression);
    }

    private Expression parseEnumDeclaration() {
        IdentifierExpression enumKeyword = matchToken(TokenType.ENUM_KEYWORD);

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

        IdentifierExpression openCurly = matchToken(TokenType.OPEN_CURLY_BRACE);

        List<IdentifierExpression> members = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_CURLY_BRACE
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN) {
            members.add(matchToken(TokenType.IDENTIFIER));
        }
        IdentifierExpression closeCurly = matchToken(TokenType.CLOSE_CURLY_BRACE);

        return new EnumDeclarationExpression(enumKeyword, identifier, openCurly, members, closeCurly);
    }

    private Expression parseUnaryExpression() {
        OpType operator = parseOpType();
        Expression operand = parseExpression();

        return new UnaryExpression(operator, operand);
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
        inTopLevel = false;
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
            inTopLevel = true;
            return new NamespaceExpression(
                    new IdentifierExpression(new Token(TokenType.NAMESPACE_KEYWORD, new Location(fileNameWithExt, -1, -1)), TokenType.NAMESPACE_KEYWORD, TokenType.NAMESPACE_KEYWORD.getText()),
                    new IdentifierExpression(new Token(TokenType.IDENTIFIER, new Location(fileNameWithExt, -1, -1), fileName), TokenType.IDENTIFIER, fileName),
                    new BlockExpression(
                            new IdentifierExpression(new Token(TokenType.OPEN_CURLY_BRACE, new Location(fileNameWithExt, -1, -1)), TokenType.OPEN_CURLY_BRACE, TokenType.OPEN_CURLY_BRACE.getText()),
                            program.getExpressions(),
                            new IdentifierExpression(new Token(TokenType.CLOSE_CURLY_BRACE, new Location(fileNameWithExt, -1, -1)), TokenType.CLOSE_CURLY_BRACE, TokenType.CLOSE_CURLY_BRACE.getText())),
                    inline
            );

        } catch (IOException e) {
            errors.add(Error.raiseImportError(path, importPath.getToken()));
        }
        inTopLevel = true;
        return new NoOpExpression();
    }

    private Expression parseMemberAccessorExpression(Expression owner) {

        IdentifierExpression accessor;
        if (current().getTokenType() == TokenType.DOT) {
            accessor = matchToken(TokenType.DOT);
        } else if (current().getTokenType() == TokenType.ARROW) {
            accessor = matchToken(TokenType.ARROW);
        } else {
            accessor = matchToken(TokenType.BAD_TOKEN);
        }

        if (current().getTokenType() == TokenType.NUM_LITERAL) {
            if (accessor.getTokenType() == TokenType.ARROW) {
                errors.add(Error.raiseUnexpectedToken(TokenType.DOT, accessor.getToken()));
            }
            IdentifierExpression numLiteral = matchToken(TokenType.NUM_LITERAL);
            LiteralExpression index = new LiteralExpression(numLiteral.getToken(), numLiteral.getValue());

            return new TupleIndexExpression(owner, accessor, index);
        }

        Expression member;
        IdentifierExpression identifierExpression = matchToken(TokenType.IDENTIFIER);
        if (current().getTokenType() == TokenType.OPEN_PARENTHESIS) { //Function call
            IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);

            List<FunctionCallArgumentExpression> arguments = parseArgumentList();

            IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

            member = new FunctionCallExpression(identifierExpression, openParen, arguments, closeParen);
        } else {
            member = identifierExpression;
        }
        MemberAccessorExpression memberAccessorExpression = new MemberAccessorExpression(owner, accessor, member);

        if (current().getTokenType() == TokenType.EQUALS) {
            IdentifierExpression equals = matchToken(TokenType.EQUALS);
            Expression assignment = parseExpression();

            return new MemberAssignmentExpression(memberAccessorExpression, equals, assignment);
        }

        if (current().getTokenType() == TokenType.DOT || current().getTokenType() == TokenType.ARROW) {
            return parseMemberAccessorExpression(memberAccessorExpression);
        }

        return memberAccessorExpression;
    }

    private Expression parseMatchExpression() {

        IdentifierExpression matchKeyword = matchToken(TokenType.MATCH_KEYWORD);
        Expression identifier = parseParenthesisedExpression();
        IdentifierExpression openCurly = matchToken(TokenType.OPEN_CURLY_BRACE);

        List<MatchCaseExpression> caseExpressions = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_CURLY_BRACE
                && current().getTokenType() != TokenType.ELSE_KEYWORD
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN) {

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

            IdentifierExpression arrow = matchToken(TokenType.THICC_ARROW);
            Expression thenExpression = parseExpression();

            //TODO: This is a concern of the rewriter, not the parser. A case could be infinitely nested ors
            if (caseExpression instanceof BinaryExpression) {
                if (((BinaryExpression) caseExpression).getOperation() == OpType.LOR) {

                    caseExpressions.add(new MatchCaseExpression(((BinaryExpression) caseExpression).getLeft(), arrow, thenExpression));
                    caseExpressions.add(new MatchCaseExpression(((BinaryExpression) caseExpression).getRight(), arrow, thenExpression));
                } else {
                    caseExpressions.add(new MatchCaseExpression(caseExpression, arrow, thenExpression));
                }
            } else {
                caseExpressions.add(new MatchCaseExpression(caseExpression, arrow, thenExpression));
            }
        }
        if (current().getTokenType() == TokenType.ELSE_KEYWORD) {
            IdentifierExpression elseKeyword = matchToken(TokenType.ELSE_KEYWORD);
            IdentifierExpression arrow = matchToken(TokenType.THICC_ARROW);
            Expression thenExpression = parseExpression();

            caseExpressions.add(new MatchCaseExpression(elseKeyword, arrow, thenExpression));
        }
        IdentifierExpression closeCurly = matchToken(TokenType.CLOSE_CURLY_BRACE);

        return new MatchExpression(matchKeyword, identifier, openCurly, caseExpressions, closeCurly);
    }

    private Expression parseFunctionCallExpression() {
        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);

        List<FunctionCallArgumentExpression> arguments = parseArgumentList();

        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        return new FunctionCallExpression(identifier, openParen, arguments, closeParen);
    }

    private List<FunctionCallArgumentExpression> parseArgumentList() {
        List<FunctionCallArgumentExpression> arguments = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_PARENTHESIS
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN) {

            IdentifierExpression refKeyword = null;
            if (current().getTokenType() == TokenType.REF_KEYWORD) {
                refKeyword = matchToken(TokenType.REF_KEYWORD);
            }

            if (current().getTokenType() == TokenType.OPEN_CURLY_BRACE) {
                arguments.add(new FunctionCallArgumentExpression(refKeyword, parseStructLiteralExpression(null)));
            } else {
                arguments.add(new FunctionCallArgumentExpression(refKeyword, parseExpression()));
            }

            if (current().getTokenType() == TokenType.CLOSE_PARENTHESIS) {
                break;
            }
            matchToken(TokenType.COMMA);
        }
        return arguments;
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

        TypeExpression typeExpression = null;
        if (current().getTokenType() == TokenType.COLON) {
            matchToken(TokenType.COLON);

            if (current().getTokenType() != TokenType.EQUALS) {
                typeExpression = parseTypeExpression();
            }
        } else if (current().getTokenType() != TokenType.EQUALS && current().getTokenType() != TokenType.IN_KEYWORD) {
            typeExpression = parseTypeExpression();
        }

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
            Expression body = parseExpression();

            return new ForExpression(forKeyword, openParen, typeExpression, identifier, equals, rangeExpression, guard, closeParen, body);

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

            return new ForInExpression(forKeyword, openParen, typeExpression, identifier, inKeyword, iterable, guard, closeParen, body);
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
        inTopLevel = false;
        IdentifierExpression structKeyword = matchToken(TokenType.STRUCT_KEYWORD);
        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

        TypeExpression typeExpression = new TypeExpression(identifier);
        if (current().getTokenType() == TokenType.OPEN_ANGLE_BRACE) {
            IdentifierExpression openAngle = matchToken(TokenType.OPEN_ANGLE_BRACE);

            List<IdentifierExpression> genericParameters = new ArrayList<>();
            genericParameters.add(matchToken(TokenType.IDENTIFIER));

            while (current().getTokenType() != TokenType.CLOSE_ANGLE_BRACE
                    && current().getTokenType() != TokenType.EOF_TOKEN
                    && current().getTokenType() != TokenType.BAD_TOKEN) {
                matchToken(TokenType.COMMA);
                genericParameters.add(matchToken(TokenType.IDENTIFIER));
            }
            IdentifierExpression closeAngle = matchToken(TokenType.CLOSE_ANGLE_BRACE);

            typeExpression = new GenericTypeExpression(identifier, openAngle, genericParameters, closeAngle);
        }
        IdentifierExpression openCurly = matchToken(TokenType.OPEN_CURLY_BRACE);

        List<Expression> members = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_CURLY_BRACE
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN
        ) {
            members.add(parseExpression());
        }
        IdentifierExpression closeCurly = matchToken(TokenType.CLOSE_CURLY_BRACE);

        inTopLevel = true;
        return new StructDeclarationExpression(structKeyword, typeExpression, openCurly, members, closeCurly);
    }

    private Expression parseFunctionDeclarationExpression() {

        inTopLevel = false;
        IdentifierExpression fnKeyword = matchToken(TokenType.FN_KEYWORD);

        List<Expression> genericParameters = new ArrayList<>();
        if (current().getTokenType() == TokenType.OPEN_ANGLE_BRACE) {
            IdentifierExpression openAngle = matchToken(TokenType.OPEN_ANGLE_BRACE);

            genericParameters.add(parseTypeExpression());
            while (current().getTokenType() != TokenType.CLOSE_ANGLE_BRACE
                    && current().getTokenType() != TokenType.EOF_TOKEN
                    && current().getTokenType() != TokenType.BAD_TOKEN) {
                matchToken(TokenType.COMMA);
                genericParameters.add(parseTypeExpression());
            }
            IdentifierExpression closeAngle = matchToken(TokenType.CLOSE_ANGLE_BRACE);
        }

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);

        List<FunctionParameterExpression> argumentExpressions = new ArrayList<>();
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
            matchToken(TokenType.COLON);
            typeExpression = parseTypeExpression();
        }

        BlockExpression body = parseBlockExpression();

        inTopLevel = true;
        return new FunctionDeclarationExpression(fnKeyword, genericParameters, identifier, openParen, argumentExpressions, closeParen, typeExpression, body);
    }

    private VariableDeclarationExpression parseVariableDeclarationExpression() {
        IdentifierExpression constKeyword = null;
        if (current().getTokenType() == TokenType.CONST_KEYWORD) {
            constKeyword = matchToken(TokenType.CONST_KEYWORD);
        }

        if (constKeyword != null && current().getTokenType() == TokenType.REF_KEYWORD) {
            errors.add(Error.raise("`ref` must precede `const`. Did you mean `ref const " + nextToken().getValue() + "`?", current()));
            matchToken(TokenType.REF_KEYWORD);
        }

        IdentifierExpression identifier = matchToken(TokenType.IDENTIFIER);

        IdentifierExpression colon = matchToken(TokenType.COLON);

        TypeExpression typeExpression = null;
        if (current().getTokenType() != TokenType.EQUALS) {
            typeExpression = parseTypeExpression();
        }

        IdentifierExpression equals = null;
        Expression initialiser = null;
        if (current().getTokenType() == TokenType.EQUALS) {
            //Variable declared but not assigned yet
            equals = matchToken(TokenType.EQUALS);

            if (current().getTokenType() == TokenType.OPEN_CURLY_BRACE) {
                initialiser = parseStructLiteralExpression(typeExpression);
            } else if (nextToken().getTokenType() == TokenType.OPEN_ANGLE_BRACE) {
                // v := Struct<T, U, V>{...}
                typeExpression = parseTypeExpression();
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

        return new VariableDeclarationExpression(constKeyword, typeExpression, colon, identifier, bar, guard, equals, initialiser);
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

        return parseAhead(new ParenthesisedExpression(openParen, expression, closeParen));
    }

    private TypeExpression parseTypeExpression() {

        Expression type;
        if (current().getTokenType() == TokenType.OPEN_PARENTHESIS) {
            IdentifierExpression openParenthesis = matchToken(TokenType.OPEN_PARENTHESIS);

            List<DelimitedExpression<TypeExpression>> delimitedExpressions = parseDelimitedList(TokenType.COMMA, this::parseTypeExpression, TokenType.CLOSE_PARENTHESIS);

            IdentifierExpression closeParenthesis = matchToken(TokenType.CLOSE_PARENTHESIS);

            type = new TupleTypeExpression(openParenthesis, delimitedExpressions, closeParenthesis);
        } else {
            type = parseTypeKeyword();
        }
        if (current().getTokenType() == TokenType.OPEN_ANGLE_BRACE) {
            IdentifierExpression openAngle = matchToken(TokenType.OPEN_ANGLE_BRACE);

            List<Expression> genericParameters = new ArrayList<>();
            genericParameters.add(parseTypeExpression());
            while (current().getTokenType() != TokenType.CLOSE_ANGLE_BRACE
                    && current().getTokenType() != TokenType.EOF_TOKEN
                    && current().getTokenType() != TokenType.BAD_TOKEN) {
                matchToken(TokenType.COMMA);
                genericParameters.add(parseTypeExpression());
            }
            IdentifierExpression closeAngle = matchToken(TokenType.CLOSE_ANGLE_BRACE);
            type = new ErasedParameterisedTypeExpression(type, openAngle, genericParameters, closeAngle);
        }
        if (current().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
            IdentifierExpression openSquareBrace = matchToken(TokenType.OPEN_SQUARE_BRACE);
            IdentifierExpression closeSquareBrace = matchToken(TokenType.CLOSE_SQUARE_BRACE);
            return new ArrayTypeExpression(type, openSquareBrace, closeSquareBrace);
        }
        return new TypeExpression(type);
    }

    private <T extends Expression> List<DelimitedExpression<T>> parseDelimitedList(TokenType delimiter, Supplier<T> supplier, TokenType terminator) {
        List<DelimitedExpression<T>> expressions = new ArrayList<>();
        while (current().getTokenType() != terminator
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN) {

            T expression = supplier.get();

            IdentifierExpression delim = null;
            if (current().getTokenType() == delimiter) {
                delim = matchToken(delimiter);
            }
            expressions.add(new DelimitedExpression<>(expression, delim));

            if (delim == null) {
                return expressions;
            }
        }
        return expressions;
    }

    private IdentifierExpression parseTypeKeyword() {
        IdentifierExpression typeKeyword;
        switch (current().getTokenType()) {
            case UNIT_KEYWORD:
                typeKeyword = matchToken(TokenType.UNIT_KEYWORD);
                break;
            case INT_KEYWORD:
                typeKeyword = matchToken(TokenType.INT_KEYWORD);
                break;
            case BOOL_KEYWORD:
                typeKeyword = matchToken(TokenType.BOOL_KEYWORD);
                break;
            case CHAR_KEYWORD:
                typeKeyword = matchToken(TokenType.CHAR_KEYWORD);
                break;
            case REAL_KEYWORD:
                typeKeyword = matchToken(TokenType.REAL_KEYWORD);
                break;
            case STRING_KEYWORD:
                typeKeyword = matchToken(TokenType.STRING_KEYWORD);
                break;
            case FUNCTION_KEYWORD:
                typeKeyword = matchToken(TokenType.FUNCTION_KEYWORD);
                break;
            case ANY_KEYWORD:
                typeKeyword = matchToken(TokenType.ANY_KEYWORD);
                break;
            case FILE_KEYWORD:
                typeKeyword = matchToken(TokenType.FILE_KEYWORD);
                break;
            default:
                typeKeyword = matchToken(TokenType.IDENTIFIER);
                break;
        }
        return typeKeyword;
    }

    private FunctionParameterExpression parseFunctionArgumentExpression() {

        IdentifierExpression refKeyword = null;
        if (current().getTokenType() == TokenType.REF_KEYWORD) {
            refKeyword = matchToken(TokenType.REF_KEYWORD);
        }

        VariableDeclarationExpression variableDeclarationExpression = parseVariableDeclarationExpression();

        if (variableDeclarationExpression.getInitialiser() != null) {
            errors.add(Error.raise("Default values for function arguments are not yet supported", variableDeclarationExpression.getEquals().getToken()));
        }

        return new FunctionParameterExpression(
                refKeyword,
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
            case ARROW:

                return parseAhead(parseMemberAccessorExpression(parsed));
            case AS_KEYWORD:
                IdentifierExpression asKeyword = matchToken(TokenType.AS_KEYWORD);

                IdentifierExpression typeKeyword;

                Expression namespaceExpression = null;
                if (current().getTokenType() == TokenType.IDENTIFIER && nextToken().getTokenType() == TokenType.COLON_COLON) {
                    namespaceExpression = parseNamespaceAccessorExpression();
                }
                typeKeyword = parseTypeKeyword();

                TypeExpression typeExpression;
                if (current().getTokenType() == TokenType.OPEN_SQUARE_BRACE) {
                    IdentifierExpression openSquareBrace = matchToken(TokenType.OPEN_SQUARE_BRACE);
                    IdentifierExpression closeSquareBrace = matchToken(TokenType.CLOSE_SQUARE_BRACE);
                    typeExpression = new ArrayTypeExpression(typeKeyword, openSquareBrace, closeSquareBrace);
                } else {
                    typeExpression = new TypeExpression(typeKeyword);
                }

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
            case OPEN_SQUARE_BRACE:
                IdentifierExpression openBrace = matchToken(TokenType.OPEN_SQUARE_BRACE);
                Expression index = parseExpression();
                IdentifierExpression closeBrace = matchToken(TokenType.CLOSE_SQUARE_BRACE);

                ArrayAccessExpression arrayAccessExpression = new ArrayAccessExpression(parsed, openBrace, index, closeBrace);
                if (current().getTokenType() != TokenType.EQUALS) {
                    return parseAhead(arrayAccessExpression);
                }
                IdentifierExpression equals = matchToken(TokenType.EQUALS);
                Expression assignment = parseExpression();

                return parseAhead(new ArrayAssignmentExpression(arrayAccessExpression, equals, assignment));
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

    private Expression parsePrintIntrinsic() {

        IdentifierExpression printInstr = matchToken(TokenType.PRINT_INTR);
        IdentifierExpression openParen = matchToken(TokenType.OPEN_PARENTHESIS);
        Expression expression = parseExpression();
        IdentifierExpression closeParen = matchToken(TokenType.CLOSE_PARENTHESIS);

        return new PrintExpression(printInstr, openParen, expression, closeParen);
    }

    private Expression parseTypeofIntrinsic() {

        IdentifierExpression typeofKeyword = matchToken(TokenType.TYPEOF_INTR);
        Expression expression = parseExpression();

        return new TypeofExpression(typeofKeyword, expression);
    }

    private Expression parseStructLiteralExpression(TypeExpression typeExpression) {
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

        StructLiteralExpression structLiteralExpression = new StructLiteralExpression(typeExpression, openCurly, expressions, closeCurly);
        if (current().getTokenType() == TokenType.DOT || current().getTokenType() == TokenType.ARROW) {
            return parseMemberAccessorExpression(structLiteralExpression);
        }
        return structLiteralExpression;
    }

    private BlockExpression parseBlockExpression() {
        IdentifierExpression openCurly = matchToken(TokenType.OPEN_CURLY_BRACE);

        List<Expression> expressions = new ArrayList<>();
        while (current().getTokenType() != TokenType.CLOSE_CURLY_BRACE
                && current().getTokenType() != TokenType.EOF_TOKEN
                && current().getTokenType() != TokenType.BAD_TOKEN) {
            expressions.add(parseExpression());
        }
        IdentifierExpression closeCurly = matchToken(TokenType.CLOSE_CURLY_BRACE);

        return new BlockExpression(openCurly, expressions, closeCurly);
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
            case XOR_KEYWORD:
                return OpType.LXOR;
            case NOT_KEYWORD:
                return OpType.NOT;
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
        position++;
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
