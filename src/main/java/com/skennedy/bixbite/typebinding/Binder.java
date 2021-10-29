package com.skennedy.bixbite.typebinding;

import com.skennedy.bixbite.exceptions.ReadOnlyVariableException;
import com.skennedy.bixbite.exceptions.TypeMismatchException;
import com.skennedy.bixbite.exceptions.UndefinedVariableException;
import com.skennedy.bixbite.lexing.model.TokenType;
import com.skennedy.bixbite.parsing.AssignmentExpression;
import com.skennedy.bixbite.parsing.BinaryExpression;
import com.skennedy.bixbite.parsing.BlockExpression;
import com.skennedy.bixbite.parsing.Expression;
import com.skennedy.bixbite.parsing.IfExpression;
import com.skennedy.bixbite.parsing.ParenthesisedExpression;
import com.skennedy.bixbite.parsing.PrintExpression;
import com.skennedy.bixbite.parsing.TypeofExpression;
import com.skennedy.bixbite.parsing.VariableDeclarationExpression;
import com.skennedy.bixbite.parsing.WhileExpression;
import com.skennedy.bixbite.parsing.model.IdentifierExpression;
import com.skennedy.bixbite.diagnostics.Error;
import com.skennedy.bixbite.exceptions.VariableAlreadyDeclaredException;
import com.skennedy.bixbite.parsing.ForExpression;
import com.skennedy.bixbite.parsing.Program;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Binder {

    private List<Error> errors;
    private BoundScope currentScope;

    public Binder() {
        currentScope = new BoundScope(null);
    }

    public BoundProgram bind(Program program) {

        errors = new ArrayList<>();

        List<BoundExpression> boundExpressions = new ArrayList<>();

        List<Expression> expressions = program.getExpressions();
        for (Expression expression : expressions) {
            boundExpressions.add(bind(expression));
        }
        return new BoundProgram(boundExpressions, errors);
    }

    public BoundExpression bind(Expression expression) {
        switch (expression.getExpressionType()) {

            case ASSIGNMENT_EXPR:
                return bindAssignmentExpression((AssignmentExpression) expression);
            case BINARY_EXPR:
                return bindBinaryExpression((BinaryExpression) expression);
            case BLOCK_EXPR:
                return bindBlockExpression((BlockExpression) expression);
            case FOR_EXPR:
                return bindForExpression((ForExpression) expression);
            case IF_EXPR:
                return bindIfExpression((IfExpression) expression);
            case LITERAL_EXPR:
                throw new IllegalStateException("Unhandled expression type: " + expression.getExpressionType());
            case PARENTHESISED_EXPR:
                return bind(((ParenthesisedExpression) expression).getExpression());
            case PRINT_EXPR:
                return bindPrintIntrinsic((PrintExpression) expression);
            case TYPEOF_EXPR:
                return bindTypeofIntrinsic((TypeofExpression) expression);
            case IDENTIFIER_EXPR:
                return bindIdentifierExpression((IdentifierExpression) expression);
            case UNARY_EXPR:
                throw new IllegalStateException("Unhandled expression type: " + expression.getExpressionType());
            case VAR_DECLARATION_EXPR:
                return bindVariableDeclaration((VariableDeclarationExpression) expression);
            case WHILE_EXPR:
                return bindWhileExpression((WhileExpression) expression);
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getExpressionType());
        }
    }

    private BoundExpression bindForExpression(ForExpression forExpression) {

        currentScope = new BoundScope(currentScope);
        BoundExpression initialiser = bind(forExpression.getInitialiser());

        TypeSymbol type = parseType(forExpression.getVarKeyword(), initialiser);

        if (!type.isAssignableFrom(initialiser.getType())) {
            errors.add(Error.raiseTypeMismatch(type, initialiser.getType()));
        }

        VariableSymbol variable = getVariableSymbol(type, forExpression.getIdentifier(), null, true);

        try {
            currentScope.declareVariable((String) forExpression.getIdentifier().getValue(), variable);
        } catch (VariableAlreadyDeclaredException vade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String)forExpression.getIdentifier().getValue()));
        }

        BoundExpression terminator = bind(forExpression.getTerminator());
        BoundExpression step = null;
        if (forExpression.getStep() != null) {
            step = bind(forExpression.getStep());
        }
        BoundExpression range = null;
        if (forExpression.getRange() != null) {
            range = bind(forExpression.getRange());
        }
        BoundExpression body = bind(forExpression.getBody());

        currentScope = currentScope.getParentScope();

        return new BoundForExpression(variable, initialiser, terminator, step, range, body);
    }

    private TypeSymbol parseType(IdentifierExpression keyword, BoundExpression initialiser) {
        switch (keyword.getTokenType()) {
            case VAR_KEYWORD:
                return initialiser.getType();
            case INT_KEYWORD:
                return TypeSymbol.INT;
            case BOOL_KEYWORD:
                return TypeSymbol.BOOL;
            case NUM_KEYWORD:
                return TypeSymbol.NUM;
            default:
                throw new IllegalStateException("Unexpected value: " + keyword.getTokenType());
        }
    }

    private VariableSymbol getVariableSymbol(TypeSymbol type, IdentifierExpression identifier, BoundExpression range, boolean readOnly) {
        return new VariableSymbol((String) identifier.getValue(), type, range, readOnly);
    }

    private BoundExpression bindIfExpression(IfExpression ifExpression) {

        BoundExpression condition = bind(ifExpression.getCondition());
        if (!condition.getType().isAssignableFrom(TypeSymbol.BOOL)) {
            errors.add(Error.raiseTypeMismatch(TypeSymbol.BOOL, condition.getType()));
            throw new TypeMismatchException(TypeSymbol.BOOL, condition.getType());
        }
        BoundExpression body = bind(ifExpression.getBody());

        BoundExpression elseBody = null;
        if (ifExpression.getElseBody() != null) {
            elseBody = bind(ifExpression.getElseBody());
        }
        return new BoundIfExpression(condition, body, elseBody);
    }

    private BoundExpression bindBinaryExpression(BinaryExpression binaryExpression) {

        BoundExpression left = bind(binaryExpression.getLeft());
        BoundExpression right = bind(binaryExpression.getRight());
        BoundBinaryOperator operator = BoundBinaryOperator.bind(binaryExpression.getOperation(), left.getType(), right.getType());

        return new BoundBinaryExpression(left, operator, right);
    }

    private BoundExpression bindIdentifierExpression(IdentifierExpression identifierExpression) {

        if (identifierExpression.getTokenType() == TokenType.INT_LITERAL) {

            return new BoundLiteralExpression(identifierExpression.getValue());
        } else if (identifierExpression.getTokenType() == TokenType.TRUE_KEYWORD) {
            return new BoundLiteralExpression(true);
        } else if (identifierExpression.getTokenType() == TokenType.FALSE_KEYWORD) {
            return new BoundLiteralExpression(false);
        }

        Optional<VariableSymbol> variable = currentScope.tryLookup((String) identifierExpression.getValue());
        if (variable.isPresent()) {
            return new BoundVariableExpression(variable.get());
        }
        throw new UndefinedVariableException((String) identifierExpression.getValue());
    }

    private BoundExpression bindPrintIntrinsic(PrintExpression printExpression) {
        BoundExpression boundExpression = bind(printExpression.getExpression());

        return new BoundPrintExpression(boundExpression);
    }

    private BoundExpression bindTypeofIntrinsic(TypeofExpression typeofExpression) {
        BoundExpression boundExpression = bind(typeofExpression.getExpression());

        return new BoundTypeofExpression(boundExpression);
    }

    private BoundExpression bindAssignmentExpression(AssignmentExpression assignmentExpression) {

        BoundExpression initialiser = bind(assignmentExpression.getAssignment());

        IdentifierExpression identifier = assignmentExpression.getIdentifier();
        Optional<VariableSymbol> scopedVariable = currentScope.tryLookup((String) identifier.getValue());
        if (!scopedVariable.isPresent()) {
            throw new UndefinedVariableException((String) identifier.getValue());
        }
        VariableSymbol variable = scopedVariable.get();
        if (variable.isReadOnly()) {
            throw new ReadOnlyVariableException(variable.getName());
        }

        if (!variable.getType().isAssignableFrom(initialiser.getType())) {
            throw new TypeMismatchException(variable.getType(), initialiser.getType());
        }

        return new BoundAssignmentExpression(variable, variable.getRange(), initialiser);
    }

    private BoundExpression bindVariableDeclaration(VariableDeclarationExpression variableDeclarationExpression) {

        BoundExpression initialiser = bind(variableDeclarationExpression.getInitialiser());

        IdentifierExpression identifier = variableDeclarationExpression.getIdentifier();

        //Create placeholder
        try {
            currentScope.declareVariable((String) identifier.getValue(), new VariableSymbol((String) identifier.getValue(), initialiser.getType(), null, false));
        } catch (VariableAlreadyDeclaredException vade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String)identifier.getValue()));
        }

        BoundExpression range = null;
        if (variableDeclarationExpression.getRange() != null) {
            range = bind(variableDeclarationExpression.getRange());
            assert range.getType().isAssignableFrom(TypeSymbol.BOOL);
        }

        TypeSymbol type = parseType(variableDeclarationExpression.getDeclarationKeyword(), initialiser);

        if (!type.isAssignableFrom(initialiser.getType())) {
            errors.add(Error.raiseTypeMismatch(type, initialiser.getType()));
        }
        VariableSymbol variable = getVariableSymbol(type, identifier, range, variableDeclarationExpression.getConstKeyword() != null);

        currentScope.reassignVariable((String) identifier.getValue(), variable);
        return new BoundVariableDeclarationExpression(variable, range, initialiser, variableDeclarationExpression.getConstKeyword() != null);
    }

    private BoundExpression bindWhileExpression(WhileExpression whileExpression) {

        BoundExpression condition = bind(whileExpression.getCondition());
        if (!condition.getType().isAssignableFrom(TypeSymbol.BOOL)) {
            throw new TypeMismatchException(TypeSymbol.BOOL, condition.getType());
        }
        BoundBlockExpression body = bindBlockExpression(whileExpression.getBody());

        return new BoundWhileExpression(condition, body);
    }

    private BoundBlockExpression bindBlockExpression(BlockExpression blockExpression) {
        List<BoundExpression> boundExpressions = new ArrayList<>();

        currentScope = new BoundScope(currentScope);

        for (Expression expression : blockExpression.getExpressions()) {
            boundExpressions.add(bind(expression));
        }

        currentScope = currentScope.getParentScope();

        return new BoundBlockExpression(boundExpressions);
    }
}
