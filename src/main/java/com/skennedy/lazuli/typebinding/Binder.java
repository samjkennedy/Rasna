package com.skennedy.lazuli.typebinding;

import com.skennedy.lazuli.diagnostics.Error;
import com.skennedy.lazuli.exceptions.FunctionAlreadyDeclaredException;
import com.skennedy.lazuli.exceptions.ReadOnlyVariableException;
import com.skennedy.lazuli.exceptions.TypeMismatchException;
import com.skennedy.lazuli.exceptions.UndefinedFunctionException;
import com.skennedy.lazuli.exceptions.UndefinedVariableException;
import com.skennedy.lazuli.exceptions.VariableAlreadyDeclaredException;
import com.skennedy.lazuli.lexing.model.TokenType;
import com.skennedy.lazuli.lowering.BoundArrayLengthExpression;
import com.skennedy.lazuli.parsing.ArrayAccessExpression;
import com.skennedy.lazuli.parsing.ArrayAssignmentExpression;
import com.skennedy.lazuli.parsing.ArrayLengthExpression;
import com.skennedy.lazuli.parsing.ArrayLiteralExpression;
import com.skennedy.lazuli.parsing.AssignmentExpression;
import com.skennedy.lazuli.parsing.BinaryExpression;
import com.skennedy.lazuli.parsing.BlockExpression;
import com.skennedy.lazuli.parsing.Expression;
import com.skennedy.lazuli.parsing.ForExpression;
import com.skennedy.lazuli.parsing.ForInExpression;
import com.skennedy.lazuli.parsing.FunctionArgumentExpression;
import com.skennedy.lazuli.parsing.FunctionCallExpression;
import com.skennedy.lazuli.parsing.FunctionDeclarationExpression;
import com.skennedy.lazuli.parsing.IfExpression;
import com.skennedy.lazuli.parsing.IncrementExpression;
import com.skennedy.lazuli.parsing.MatchCaseExpression;
import com.skennedy.lazuli.parsing.MatchExpression;
import com.skennedy.lazuli.parsing.ParenthesisedExpression;
import com.skennedy.lazuli.parsing.PrintExpression;
import com.skennedy.lazuli.parsing.Program;
import com.skennedy.lazuli.parsing.ReturnExpression;
import com.skennedy.lazuli.parsing.TypeofExpression;
import com.skennedy.lazuli.parsing.VariableDeclarationExpression;
import com.skennedy.lazuli.parsing.WhileExpression;
import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;

import java.util.ArrayList;
import java.util.Iterator;
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

            case ARRAY_LITERAL_EXPR:
                return bindArrayLiteralExpression((ArrayLiteralExpression) expression);
            case ARRAY_ACCESS_EXPR:
                return bindArrayAccessExpression((ArrayAccessExpression) expression);
            case ASSIGNMENT_EXPR:
                return bindAssignmentExpression((AssignmentExpression) expression);
            case BINARY_EXPR:
                return bindBinaryExpression((BinaryExpression) expression);
            case BLOCK_EXPR:
                return bindBlockExpression((BlockExpression) expression);
            case FOR_EXPR:
                return bindForExpression((ForExpression) expression);
            case FOR_IN_EXPR:
                return bindForInExpression((ForInExpression) expression);
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
            case INCREMENT_EXPR:
                return bindIncrementExpression((IncrementExpression) expression);
            case UNARY_EXPR:
                throw new IllegalStateException("Unhandled expression type: " + expression.getExpressionType());
            case VAR_DECLARATION_EXPR:
                return bindVariableDeclaration((VariableDeclarationExpression) expression);
            case WHILE_EXPR:
                return bindWhileExpression((WhileExpression) expression);
            case FUNC_DECLARATION_EXPR:
                return bindFunctionDeclarationExpression(((FunctionDeclarationExpression) expression));
            case FUNC_CALL_EXPR:
                return bindFunctionCallExpression((FunctionCallExpression) expression);
            case RETURN_EXPR:
                return bindReturnExpression((ReturnExpression) expression);
            case MATCH_EXPRESSION:
                return bindMatchExpression((MatchExpression) expression);
            case ARRAY_LEN_EXPR:
                return bindArrayLengthExpression((ArrayLengthExpression) expression);
            case ARRAY_ASSIGNMENT_EXPR:
                return bindArrayAssignmentExpression((ArrayAssignmentExpression) expression);
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getExpressionType());
        }
    }

    private BoundExpression bindMatchExpression(MatchExpression matchExpression) {

        BoundExpression operand = bind(matchExpression.getOperand());

        List<BoundMatchCaseExpression> boundMatchCaseExpressions = new ArrayList<>();

        TypeSymbol type = null;
        for (MatchCaseExpression caseExpression : matchExpression.getCaseExpressions()) {
            BoundMatchCaseExpression boundMatchCaseExpression = bindCaseExpression(caseExpression);
            if (type != null && !type.isAssignableFrom(boundMatchCaseExpression.getType())) {
                throw new TypeMismatchException(type, boundMatchCaseExpression.getType());
            }
            type = boundMatchCaseExpression.getType();
            boundMatchCaseExpressions.add(boundMatchCaseExpression);
        }

        return new BoundMatchExpression(type, operand, boundMatchCaseExpressions);
    }

    private BoundMatchCaseExpression bindCaseExpression(MatchCaseExpression matchCaseExpression) {

        BoundExpression caseExpression;
        if (matchCaseExpression.getCaseExpression().getExpressionType() == ExpressionType.IDENTIFIER_EXPR
                && ((IdentifierExpression) matchCaseExpression.getCaseExpression()).getTokenType() == TokenType.ELSE_KEYWORD) {
            caseExpression = null; //TODO: Is this the best way to denote a default case?
        } else {
            caseExpression = bind(matchCaseExpression.getCaseExpression());
        }
        BoundExpression boundThenExpression = bind(matchCaseExpression.getThenExpression());

        return new BoundMatchCaseExpression(caseExpression, boundThenExpression);
    }

    private BoundExpression bindReturnExpression(ReturnExpression returnExpression) {
        BoundExpression returnValue = bind(returnExpression.getReturnValue());

        return new BoundReturnExpression(returnValue);
    }

    private BoundExpression bindArrayLiteralExpression(ArrayLiteralExpression arrayLiteralExpression) {

        List<BoundExpression> boundElements = new ArrayList<>();
        for (Expression element : arrayLiteralExpression.getElements()) {
            boundElements.add(bind(element));
        }

        return new BoundArrayLiteralExpression(boundElements);
    }

    private BoundArrayAccessExpression bindArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) {
        IdentifierExpression identifier = arrayAccessExpression.getIdentifier();
        Optional<VariableSymbol> variable = currentScope.tryLookupVariable((String) identifier.getValue());
        if (variable.isEmpty()) {
            throw new UndefinedVariableException((String) identifier.getValue());
        }
        if (!variable.get().getType().isAssignableFrom(TypeSymbol.INT_ARRAY)) {
            errors.add(Error.raiseTypeMismatch(TypeSymbol.INT_ARRAY, variable.get().getType()));
        }
        BoundExpression index = bind(arrayAccessExpression.getIndex());

        if (!index.getType().isAssignableFrom(TypeSymbol.INT)) {
            errors.add(Error.raiseTypeMismatch(TypeSymbol.INT, index.getType()));
        }
        return new BoundArrayAccessExpression(new BoundVariableExpression(variable.get()), index);
    }

    private BoundExpression bindArrayLengthExpression(ArrayLengthExpression arrayLengthExpression) {

        BoundExpression boundExpression = bind(arrayLengthExpression.getExpression());
        if (!boundExpression.getType().isAssignableFrom(TypeSymbol.INT_ARRAY)) {
            errors.add(Error.raiseTypeMismatch(TypeSymbol.INT_ARRAY, boundExpression.getType()));
        }

        return new BoundArrayLengthExpression(boundExpression);
    }

    private BoundExpression bindArrayAssignmentExpression(ArrayAssignmentExpression expression) {

        BoundArrayAccessExpression boundArrayAccessExpression = bindArrayAccessExpression(expression.getArrayAccessExpression());
        BoundExpression assignment = bind(expression.getAssignment());

        return new BoundArrayAssignmentExpression(boundArrayAccessExpression, assignment);
    }

    private BoundExpression bindForExpression(ForExpression forExpression) {

        currentScope = new BoundScope(currentScope);
        BoundExpression initialiser = bind(forExpression.getInitialiser());

        TypeSymbol type = parseType(forExpression.getTypeKeyword());

        if (!type.isAssignableFrom(initialiser.getType())) {
            errors.add(Error.raiseTypeMismatch(type, initialiser.getType()));
        }

        VariableSymbol variable = getVariableSymbol(type, forExpression.getIdentifier(), null, true);

        try {
            currentScope.declareVariable((String) forExpression.getIdentifier().getValue(), variable);
        } catch (VariableAlreadyDeclaredException vade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String) forExpression.getIdentifier().getValue()));
        }

        BoundExpression terminator = bind(forExpression.getTerminator());
        BoundExpression step = null;
        if (forExpression.getStep() != null) {
            step = bind(forExpression.getStep());
        }
        BoundExpression guard = null;
        if (forExpression.getGuard() != null) {
            guard = bind(forExpression.getGuard());
        }
        BoundExpression body = bind(forExpression.getBody());

        currentScope = currentScope.getParentScope();

        return new BoundForExpression(variable, initialiser, terminator, step, guard, body);
    }

    private BoundExpression bindForInExpression(ForInExpression forInExpression) {
        currentScope = new BoundScope(currentScope);

        BoundExpression iterable = bind(forInExpression.getIterable());
        if (!iterable.getType().isAssignableFrom(TypeSymbol.INT_ARRAY)) {
            throw new IllegalStateException("For-in expression only applicable to Array type");
        }

        TypeSymbol type = parseType(forInExpression.getTypeKeyword());

        //TODO: Type check array type
//        if (!type.isAssignableFrom(iterable.getType())) {
//            errors.add(Error.raiseTypeMismatch(type, iterable.getType()));
//        }
        VariableSymbol variable = getVariableSymbol(type, forInExpression.getIdentifier(), null, true);

        try {
            currentScope.declareVariable((String) forInExpression.getIdentifier().getValue(), variable);
        } catch (VariableAlreadyDeclaredException vade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String) forInExpression.getIdentifier().getValue()));
        }

        BoundExpression guard = null;
        if (forInExpression.getGuard() != null) {
            guard = bind(forInExpression.getGuard());
        }

        BoundExpression body = bind(forInExpression.getBody());

        currentScope = currentScope.getParentScope();

        return new BoundForInExpression(variable, iterable, guard, body);
    }

    private TypeSymbol parseType(IdentifierExpression keyword) {
        switch (keyword.getTokenType()) {
            case VOID_KEYWORD:
                return TypeSymbol.VOID;
            case INT_KEYWORD:
                return TypeSymbol.INT;
            case INT_ARRAY_KEYWORD:
                return TypeSymbol.INT_ARRAY;
            case BOOL_KEYWORD:
                return TypeSymbol.BOOL;
            case REAL_KEYWORD:
                return TypeSymbol.REAL;
            case FUNCTION_TYPE_KEYWORD:
                return TypeSymbol.FUNCTION;
            default:
                throw new IllegalStateException("Unexpected value: " + keyword.getTokenType());
        }
    }

    private VariableSymbol getVariableSymbol(TypeSymbol type, IdentifierExpression identifier, BoundExpression guard, boolean readOnly) {
        return new VariableSymbol((String) identifier.getValue(), type, guard, readOnly);
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

        Optional<VariableSymbol> variable = currentScope.tryLookupVariable((String) identifierExpression.getValue());
        if (variable.isPresent()) {
            return new BoundVariableExpression(variable.get());
        }
        throw new UndefinedVariableException((String) identifierExpression.getValue());
    }

    private BoundExpression bindIncrementExpression(IncrementExpression incrementExpression) {

        Optional<VariableSymbol> variable = currentScope.tryLookupVariable((String) incrementExpression.getIdentifier().getValue());
        if (variable.isEmpty()) {
            throw new UndefinedVariableException((String) incrementExpression.getIdentifier().getValue());
        }

        int increment;
        if (incrementExpression.getOperator().getTokenType() == TokenType.INCREMENT) {
            increment = 1;
        } else if (incrementExpression.getOperator().getTokenType() == TokenType.DECREMENT) {
            increment = -1;
        } else {
            throw new IllegalStateException("Unknown operator in increment expression: " + incrementExpression.getOperator().getTokenType());
        }

        return new BoundIncrementExpression(variable.get(), new BoundLiteralExpression(increment));
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
        Optional<VariableSymbol> scopedVariable = currentScope.tryLookupVariable((String) identifier.getValue());
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

        return new BoundAssignmentExpression(variable, variable.getGuard(), initialiser);
    }

    private BoundExpression bindFunctionDeclarationExpression(FunctionDeclarationExpression functionDeclarationExpression) {

        IdentifierExpression identifier = functionDeclarationExpression.getIdentifier();

        TypeSymbol type = parseType(functionDeclarationExpression.getTypeKeyword());

        currentScope = new BoundScope(currentScope);

        //Declare the arguments within the function's scope
        List<BoundFunctionArgumentExpression> arguments = new ArrayList<>();
        for (FunctionArgumentExpression argumentExpression : functionDeclarationExpression.getArguments()) {
            arguments.add(bindFunctionArgumentExpression(argumentExpression));
        }

        FunctionSymbol functionSymbol = new FunctionSymbol((String) identifier.getValue(), type, arguments, null);
        try {
            //Declare the function in the parent scope
            currentScope.getParentScope().declareFunction((String) identifier.getValue(), functionSymbol);
        } catch (FunctionAlreadyDeclaredException fade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String) identifier.getValue()));
        }

        BoundBlockExpression body = bindBlockExpression(functionDeclarationExpression.getBody());

        if (functionSymbol.getType() != TypeSymbol.VOID) {
            analyzeBody(functionSymbol, body.getExpressions().iterator());
        }

        currentScope = currentScope.getParentScope();

        return new BoundFunctionDeclarationExpression(functionSymbol, arguments, body);
    }

    private void analyzeBody(FunctionSymbol function, Iterator<BoundExpression> expressions) {

        //BFS the body to ensure all paths return a value of the right type
        for (Iterator<BoundExpression> it = expressions; it.hasNext(); ) {
            BoundExpression expression = it.next();
            if (expression == null) {
                return;
            }
            if (expression.getBoundExpressionType() == BoundExpressionType.RETURN) {
                if (expression.getType() != function.getType()) {
                    throw new TypeMismatchException(function.getType(), expression.getType());
                }
                return;
            }
        }
    }

    private BoundFunctionArgumentExpression bindFunctionArgumentExpression(FunctionArgumentExpression argumentExpression) {

        IdentifierExpression identifier = argumentExpression.getIdentifier();
        TypeSymbol type = parseType(argumentExpression.getTypeKeyword());

        //Create placeholder
        try {
            currentScope.declareVariable((String) identifier.getValue(), new VariableSymbol((String) identifier.getValue(), type, null, false));
        } catch (VariableAlreadyDeclaredException vade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String) identifier.getValue()));
        }

        BoundExpression guard = null;
        if (argumentExpression.getGuard() != null) {
            guard = bind(argumentExpression.getGuard());
        }

        VariableSymbol argument = getVariableSymbol(type, identifier, guard, argumentExpression.getConstKeyword() != null);

        currentScope.reassignVariable((String) identifier.getValue(), argument);

        return new BoundFunctionArgumentExpression(argument, guard);
    }

    private BoundExpression bindFunctionCallExpression(FunctionCallExpression functionCallExpression) {

        IdentifierExpression identifier = functionCallExpression.getIdentifier();
        Optional<FunctionSymbol> scopedFunction = currentScope.tryLookupFunction((String) identifier.getValue());
        if (scopedFunction.isEmpty()) {
            throw new UndefinedFunctionException((String) identifier.getValue());
        }
        FunctionSymbol function = scopedFunction.get();

        List<BoundExpression> boundArguments = new ArrayList<>();
        List<BoundFunctionArgumentExpression> arguments = function.getArguments();
        List<Expression> functionCallExpressionArguments = functionCallExpression.getArguments();
        for (int i = 0; i < functionCallExpressionArguments.size(); i++) {
            BoundExpression boundArgument = bind(functionCallExpressionArguments.get(i));
            if (!arguments.get(i).getType().isAssignableFrom(boundArgument.getType())) {
                throw new TypeMismatchException(arguments.get(i).getType(), boundArgument.getType());
            }
            boundArguments.add(boundArgument);
        }

        return new BoundFunctionCallExpression(function, boundArguments);
    }

    private BoundExpression bindVariableDeclaration(VariableDeclarationExpression variableDeclarationExpression) {

        BoundExpression initialiser = null;
        if (variableDeclarationExpression.getInitialiser() != null) {
            initialiser = bind(variableDeclarationExpression.getInitialiser());
        }

        IdentifierExpression identifier = variableDeclarationExpression.getIdentifier();

        //Create placeholder
        try {
            TypeSymbol type = parseType(variableDeclarationExpression.getTypeKeyword());
            currentScope.declareVariable((String) identifier.getValue(), new VariableSymbol((String) identifier.getValue(), type, null, false));
        } catch (VariableAlreadyDeclaredException vade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String) identifier.getValue()));
        }

        BoundExpression guard = null;
        if (variableDeclarationExpression.getGuard() != null) {
            guard = bind(variableDeclarationExpression.getGuard());
            assert guard.getType().isAssignableFrom(TypeSymbol.BOOL);
        }

        //TODO: Array of what?
        TypeSymbol type = variableDeclarationExpression.isArray() ? TypeSymbol.INT_ARRAY : parseType(variableDeclarationExpression.getTypeKeyword());

        if (initialiser != null && !type.isAssignableFrom(initialiser.getType())) {
            errors.add(Error.raiseTypeMismatch(type, initialiser.getType()));
        }
        VariableSymbol variable = getVariableSymbol(type, identifier, guard, variableDeclarationExpression.getConstKeyword() != null);

        currentScope.reassignVariable((String) identifier.getValue(), variable);
        return new BoundVariableDeclarationExpression(variable, guard, initialiser, variableDeclarationExpression.getConstKeyword() != null);
    }

    private BoundExpression bindWhileExpression(WhileExpression whileExpression) {

        BoundExpression condition = bind(whileExpression.getCondition());
        if (!condition.getType().isAssignableFrom(TypeSymbol.BOOL)) {
            throw new TypeMismatchException(TypeSymbol.BOOL, condition.getType());
        }

        BoundExpression body = bind(whileExpression.getBody());

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
