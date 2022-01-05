package com.skennedy.lazuli.typebinding;

import com.skennedy.lazuli.diagnostics.BindingError;
import com.skennedy.lazuli.exceptions.FunctionAlreadyDeclaredException;
import com.skennedy.lazuli.exceptions.InvalidOperationException;
import com.skennedy.lazuli.exceptions.ReadOnlyVariableException;
import com.skennedy.lazuli.exceptions.TypeAlreadyDeclaredException;
import com.skennedy.lazuli.exceptions.TypeMismatchException;
import com.skennedy.lazuli.exceptions.UndefinedVariableException;
import com.skennedy.lazuli.exceptions.VariableAlreadyDeclaredException;
import com.skennedy.lazuli.lexing.model.TokenType;
import com.skennedy.lazuli.lowering.BoundArrayLengthExpression;
import com.skennedy.lazuli.lowering.BoundNoOpExpression;
import com.skennedy.lazuli.parsing.*;
import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class Binder {

    private List<BindingError> errors;
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
            case MEMBER_ASSIGNMENT_EXPR:
                return bindMemberAssignmentExpression((MemberAssignmentExpression) expression);
            case TUPLE_LITERAL_EXPR:
                return bindTupleLiteralExpression((TupleLiteralExpression) expression);
            case LAMBDA_EXPRESSION:
                return bindLambdaExpression((LambdaExpression) expression);
            case MAP_EXPRESSION:
                return bindMapExpression((MapExpression) expression);
            case ARRAY_DECLARATION_EXPR:
                return bindArrayDeclarationExpression((ArrayDeclarationExpression) expression);
            case YIELD_EXPRESSION:
                return bindYieldExpression((YieldExpression) expression);
            case STRUCT_DECLARATION_EXPR:
                return bindStructDeclarationExpression((StructDeclarationExpression) expression);
            case MEMBER_ACCESSOR_EXPR:
                return bindMemberAccessorExpression((MemberAccessorExpression) expression);
            case NAMESPACE:
                return bindNamespace((NamespaceExpression) expression);
            case NAMESPACE_ACCESSOR_EXPR:
                return bindNamespaceAccessorExpression((NamespaceAccessorExpression) expression);
            case CAST_EXPR:
                return bindCastExpression((CastExpression) expression);
            case STRUCT_LITERAL_EXPRESSION:
                return bindStructLiteralExpression((StructLiteralExpression) expression);
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getExpressionType());
        }
    }

    private BoundExpression bindStructLiteralExpression(StructLiteralExpression structLiteralExpression) {

        Optional<TypeSymbol> type = currentScope.tryLookupType((String) structLiteralExpression.getTypeExpression().getIdentifier().getValue());
        if (type.isEmpty()) {
            //This should never happen
            errors.add(BindingError.raiseUnknownType((String) structLiteralExpression.getTypeExpression().getIdentifier().getValue(), structLiteralExpression.getSpan()));
            return new BoundNoOpExpression(); //TODO: BoundExceptionExpression
        }

        Optional<FunctionSymbol> constructor = currentScope.tryLookupFunction((String) structLiteralExpression.getTypeExpression().getIdentifier().getValue());
        if (constructor.isEmpty()) {
            //This should never happen
            errors.add(BindingError.raiseUnknownType((String) structLiteralExpression.getTypeExpression().getIdentifier().getValue(), structLiteralExpression.getSpan()));
            return new BoundNoOpExpression(); //TODO: BoundExceptionExpression
        }

        List<BoundExpression> members = new ArrayList<>();
        List<Expression> structLiteralExpressionMembers = structLiteralExpression.getMembers();
        for (Expression member : structLiteralExpressionMembers) {
            members.add(bind(member));
        }

        if (constructor.get().getArguments().size() != structLiteralExpression.getMembers().size()) {
            //This could be a little misleading, need signature info in the functionsymbol
            errors.add(BindingError.raiseUnknownFunction((String) structLiteralExpression.getTypeExpression().getIdentifier().getValue(), members, structLiteralExpression.getSpan()));
            return new BoundNoOpExpression(); //TODO: BoundExceptionExpression
        }


        for (int i = 0; i < structLiteralExpressionMembers.size(); i++) {
            BoundExpression boundMember = members.get(i);
            TypeSymbol expectedType = constructor.get().getArguments().get(i).getType();
            if (!expectedType.isAssignableFrom(boundMember.getType())) {

                //TODO: Can be argType mismatch, e.g. expected type X in position Y of function Z
                errors.add(BindingError.raiseTypeMismatch(expectedType, boundMember.getType(), structLiteralExpression.getMembers().get(i).getSpan()));
            }
        }

        if (constructor.get().getArguments().size() != members.size()) {
            //This could be a little misleading, need signature info in the functionsymbol
            errors.add(BindingError.raiseUnknownFunction((String) structLiteralExpression.getTypeExpression().getIdentifier().getValue(), members, structLiteralExpression.getSpan()));
            return new BoundNoOpExpression(); //TODO: BoundExceptionExpression
        }

        return new BoundStructLiteralExpression(type.get(), members);
    }

    private BoundExpression bindCastExpression(CastExpression castExpression) {

        BoundExpression boundExpression = bind(castExpression.getExpression());
        TypeSymbol type = parseType(castExpression.getType());

        return new BoundCastExpression(boundExpression, type);
    }

    private BoundExpression bindNamespaceAccessorExpression(NamespaceAccessorExpression namespaceAccessorExpression) {
        IdentifierExpression namespace = namespaceAccessorExpression.getNamespace();

        Optional<BoundScope> namespaceScope = currentScope.tryLookupNamespace((String) namespace.getValue());
        if (namespaceScope.isEmpty()) {
            errors.add(BindingError.raiseUnknownNamespace((String) namespace.getValue(), namespaceAccessorExpression.getSpan()));
            throw new IllegalStateException("Unknown namespace: `" + namespace.getValue() + "`"); //TODO: Return a BoundExceptionExpression in future
        }
        BoundScope previous = currentScope;
        currentScope = BoundScope.merge(previous, namespaceScope.get());

        BoundExpression boundExpression = bind(namespaceAccessorExpression.getExpression());

        currentScope = previous;

        return boundExpression;
    }

    private BoundExpression bindNamespace(NamespaceExpression namespaceExpression) {

        if (namespaceExpression.isInline()) {
            List<BoundExpression> boundExpressions = new ArrayList<>();

            for (Expression expression : namespaceExpression.getBody().getExpressions()) {
                boundExpressions.add(bind(expression));
            }
            currentScope.declareNamespace((String) namespaceExpression.getNamespace().getValue(), currentScope);

            return new BoundBlockExpression(boundExpressions);
        }

        currentScope = new BoundScope(currentScope);

        List<BoundExpression> boundExpressions = new ArrayList<>();

        for (Expression expression : namespaceExpression.getBody().getExpressions()) {
            boundExpressions.add(bind(expression));
        }
        BoundScope namespaceScope = currentScope;

        currentScope = currentScope.getParentScope();

        currentScope.declareNamespace((String) namespaceExpression.getNamespace().getValue(), namespaceScope);

        return new BoundBlockExpression(boundExpressions);
    }

    private BoundMemberAccessorExpression bindMemberAccessorExpression(MemberAccessorExpression memberAccessorExpression) {
        BoundExpression boundOwner = bind(memberAccessorExpression.getOwner());

        IdentifierExpression member = memberAccessorExpression.getMember();

        BoundExpression boundMember;

        Optional<TypeSymbol> typeSymbol = currentScope.tryLookupType(boundOwner.getType().getName());
        if (typeSymbol.isEmpty()) {
            throw new IllegalStateException("No such type " + boundOwner.getType().getName() + " in scope, possibly a parser bug");
        }
        TypeSymbol type = typeSymbol.get();
        if (type.getFunctions().containsKey(member.getValue())) {
            throw new UnsupportedOperationException("Function reference accessors are not yet supported");
        }

        if (!type.getFields().containsKey(member.getValue())) {
            //TODO: Better error reporting
            errors.add(BindingError.raiseUnknownMember((String) member.getValue(), type, memberAccessorExpression.getMember().getSpan()));
        }

        VariableSymbol variable = type.getFields().get(member.getValue());
        boundMember = new BoundVariableExpression(variable);

        return new BoundMemberAccessorExpression(boundOwner, boundMember);
    }

    private BoundExpression bindYieldExpression(YieldExpression yieldExpression) {
        BoundExpression expression = bind(yieldExpression.getExpression());

        return new BoundYieldExpression(expression);
    }

    private BoundExpression bindArrayDeclarationExpression(ArrayDeclarationExpression arrayDeclarationExpression) {
        BoundExpression elementCount = bind(arrayDeclarationExpression.getElementCount());
        if (!elementCount.getType().isAssignableFrom(TypeSymbol.INT)) {
            throw new IllegalStateException("Element count must be of type int");
        }
        ArrayTypeSymbol typeSymbol = new ArrayTypeSymbol(parseType(arrayDeclarationExpression.getTypeExpression()));

        return new BoundArrayDeclarationExpression(typeSymbol, elementCount);
    }

    private BoundExpression bindMapExpression(MapExpression mapExpression) {
        BoundExpression boundMapperFunction = bind(mapExpression.getMapFunction());
        if (!boundMapperFunction.getType().isAssignableFrom(TypeSymbol.FUNCTION)) {
            throw new IllegalStateException("Map must take a function");
        }
        BoundExpression boundOperand = bind(mapExpression.getExpression());

        if (!(boundOperand.getType() instanceof ArrayTypeSymbol)) {
            throw new IllegalStateException("Map can only operate on array types");
        }
        return new BoundMapExpression(boundMapperFunction, boundOperand);
    }

    private BoundExpression bindTupleLiteralExpression(TupleLiteralExpression tupleLiteralExpression) {

        List<BoundExpression> boundElements = new ArrayList<>();
        for (Expression element : tupleLiteralExpression.getElements()) {
            boundElements.add(bind(element));
        }

        return new BoundTupleLiteralExpression(boundElements);
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
            if (boundMatchCaseExpression.getCaseExpression() != null && boundMatchCaseExpression.getCaseExpression().getType() != operand.getType()) {
                throw new TypeMismatchException(operand.getType(), boundMatchCaseExpression.getCaseExpression().getType());
            }
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

    private BoundPositionalAccessExpression bindArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) {
        IdentifierExpression identifier = arrayAccessExpression.getIdentifier();
        Optional<VariableSymbol> variable = currentScope.tryLookupVariable((String) identifier.getValue());
        if (variable.isEmpty()) {
            throw new UndefinedVariableException((String) identifier.getValue());
        }

        BoundExpression index = bind(arrayAccessExpression.getIndex());

        if (!index.getType().isAssignableFrom(TypeSymbol.INT)) {
            errors.add(BindingError.raiseTypeMismatch(TypeSymbol.INT, index.getType(), arrayAccessExpression.getIndex().getSpan()));
        }
        return new BoundPositionalAccessExpression(new BoundVariableExpression(variable.get()), index);
    }

    private BoundExpression bindArrayLengthExpression(ArrayLengthExpression arrayLengthExpression) {

        BoundExpression boundExpression = bind(arrayLengthExpression.getExpression());
//        if (!boundExpression.getType().isAssignableFrom(TypeSymbol.INT_ARRAY) && boundExpression.getType() != TypeSymbol.TUPLE) {
//            errors.add(Error.raiseTypeMismatch(TypeSymbol.INT_ARRAY, boundExpression.getType()));
//        }

        return new BoundArrayLengthExpression(boundExpression);
    }

    private BoundExpression bindArrayAssignmentExpression(ArrayAssignmentExpression expression) {

        BoundPositionalAccessExpression boundArrayAccessExpression = bindArrayAccessExpression(expression.getArrayAccessExpression());
        BoundExpression assignment = bind(expression.getAssignment());

        return new BoundArrayAssignmentExpression(boundArrayAccessExpression, assignment);
    }

    private BoundExpression bindMemberAssignmentExpression(MemberAssignmentExpression memberAssignmentExpression) {

        BoundMemberAccessorExpression boundMemberAccessorExpression = bindMemberAccessorExpression(memberAssignmentExpression.getMemberAccessorExpression());
        BoundExpression assignment = bind(memberAssignmentExpression.getAssignment());

        if (!boundMemberAccessorExpression.getMember().getType().isAssignableFrom(assignment.getType())) {
            errors.add(BindingError.raiseTypeMismatch(boundMemberAccessorExpression.getMember().getType(), assignment.getType(), memberAssignmentExpression.getAssignment().getSpan()));
        }

        if (boundMemberAccessorExpression.getMember() instanceof BoundVariableExpression) { //Always true right now
            BoundVariableExpression variableExpression = (BoundVariableExpression) boundMemberAccessorExpression.getMember();
            if (variableExpression.getVariable().isReadOnly()) {
                errors.add(BindingError.raiseConstReassignmentError(variableExpression.getVariable(), memberAssignmentExpression.getSpan()));
                return new BoundMemberAssignmentExpression(boundMemberAccessorExpression, assignment);
            }
        }

        return new BoundMemberAssignmentExpression(boundMemberAccessorExpression, assignment);
    }

    private BoundExpression bindForExpression(ForExpression forExpression) {

        currentScope = new BoundScope(currentScope);
        BoundRangeExpression range = bindRangeExpression(forExpression.getRangeExpression());

        TypeSymbol type = parseType(forExpression.getTypeExpression());

        if (!type.isAssignableFrom(range.getType())) {
            errors.add(BindingError.raiseTypeMismatch(type, range.getType(), forExpression.getRangeExpression().getSpan()));
        }

        VariableSymbol variable = buildVariableSymbol(type, forExpression.getIdentifier(), null, true, forExpression);

        try {
            currentScope.declareVariable((String) forExpression.getIdentifier().getValue(), variable);
        } catch (VariableAlreadyDeclaredException vade) {
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable((String) forExpression.getIdentifier().getValue()).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, forExpression.getIdentifier().getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
        }
        BoundExpression guard = null;
        if (forExpression.getGuard() != null) {
            guard = bind(forExpression.getGuard());
        }
        BoundExpression body = bind(forExpression.getBody());

        currentScope = currentScope.getParentScope();

        return new BoundForExpression(variable, range, guard, body);
    }

    private BoundRangeExpression bindRangeExpression(RangeExpression rangeExpression) {

        BoundExpression lowerBound = bind(rangeExpression.getLowerBound());
        BoundExpression upperBound = bind(rangeExpression.getUpperBound());

        BoundExpression step = null;
        if (rangeExpression.getStep() != null) {
            step = bind(rangeExpression.getStep());
        }

        return new BoundRangeExpression(lowerBound, upperBound, step);
    }

    private BoundExpression bindForInExpression(ForInExpression forInExpression) {
        currentScope = new BoundScope(currentScope);

        BoundExpression iterable = bind(forInExpression.getIterable());
//        if (!iterable.getType().isAssignableFrom(TypeSymbol.INT_ARRAY) && iterable.getType() != TypeSymbol.TUPLE) {
//            throw new IllegalStateException("For-in expression only applicable to Array or Tuple types");
//        }

        TypeSymbol type = parseType(forInExpression.getTypeExpression());

        if (!type.isAssignableFrom(iterable.getType())) {
            errors.add(BindingError.raiseTypeMismatch(type, iterable.getType(), forInExpression.getIterable().getSpan()));
        }
        VariableSymbol variable = buildVariableSymbol(type, forInExpression.getIdentifier(), null, false, forInExpression);

        try {
            currentScope.declareVariable((String) forInExpression.getIdentifier().getValue(), variable);
        } catch (VariableAlreadyDeclaredException vade) {
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable((String) forInExpression.getIdentifier().getValue()).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, forInExpression.getIdentifier().getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
        }

        BoundExpression guard = null;
        if (forInExpression.getGuard() != null) {
            guard = bind(forInExpression.getGuard());
        }

        BoundExpression body = bind(forInExpression.getBody());

        currentScope = currentScope.getParentScope();

        return new BoundForInExpression(variable, iterable, guard, body);
    }

    private TypeSymbol parseType(TypeExpression typeExpression) {
        TypeSymbol typeSymbol;

        if (typeExpression == null) {
            return TypeSymbol.VOID;
        }

        switch (typeExpression.getIdentifier().getTokenType()) {
            case INT_KEYWORD:
                typeSymbol = TypeSymbol.INT;
                break;
            case BOOL_KEYWORD:
                typeSymbol = TypeSymbol.BOOL;
                break;
            case STRING_KEYWORD:
                typeSymbol = TypeSymbol.STRING;
                break;
            case REAL_KEYWORD:
                typeSymbol = TypeSymbol.REAL;
                break;
            case FUNCTION_KEYWORD:
                typeSymbol = TypeSymbol.FUNCTION;
                break;
            case TUPLE_KEYWORD:
                typeSymbol = TypeSymbol.TUPLE;
                break;
            case ANY_KEYWORD:
                typeSymbol = TypeSymbol.ANY;
                break;
            default:
                Optional<TypeSymbol> type = currentScope.tryLookupType((String) typeExpression.getIdentifier().getValue());
                if (type.isEmpty()) {
                    errors.add(BindingError.raiseUnknownType((String) typeExpression.getIdentifier().getValue(), typeExpression.getIdentifier().getSpan()));
                    typeSymbol = null;
                } else {
                    typeSymbol = type.get();
                }
        }
        //TODO: This doesn't do N-Dimensional arrays yet
        if (typeExpression.getOpenSquareBracket() != null && typeExpression.getCloseSquareBracket() != null) {
            return new ArrayTypeSymbol(typeSymbol);
        }
        return typeSymbol;
    }

    private VariableSymbol buildVariableSymbol(TypeSymbol type, IdentifierExpression identifier, BoundExpression guard, boolean readOnly, Expression declaration) {
        return new VariableSymbol((String) identifier.getValue(), type, guard, readOnly, declaration);
    }

    private BoundExpression bindIfExpression(IfExpression ifExpression) {

        BoundExpression condition = bind(ifExpression.getCondition());
        if (!condition.getType().isAssignableFrom(TypeSymbol.BOOL)) {
            errors.add(BindingError.raiseTypeMismatch(TypeSymbol.BOOL, condition.getType(), ifExpression.getCondition().getSpan()));
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
        try {
            BoundBinaryOperator operator = BoundBinaryOperator.bind(binaryExpression.getOperation(), left.getType(), right.getType());
            return new BoundBinaryExpression(left, operator, right);
        } catch (InvalidOperationException ioe) {
            errors.add(BindingError.raiseInvalidOperationException(binaryExpression.getOperation(), left.getType(), right.getType(), binaryExpression.getSpan()));
            return new BoundBinaryExpression(left, null, right);
        }
    }

    private BoundExpression bindIdentifierExpression(IdentifierExpression identifierExpression) {

        if (identifierExpression.getTokenType() == TokenType.INT_LITERAL || identifierExpression.getTokenType() == TokenType.STRING_LITERAL) {
            return new BoundLiteralExpression(identifierExpression.getValue());

        } else if (identifierExpression.getTokenType() == TokenType.TRUE_KEYWORD) {
            return new BoundLiteralExpression(true);

        } else if (identifierExpression.getTokenType() == TokenType.FALSE_KEYWORD) {
            return new BoundLiteralExpression(false);
        }

        Optional<TypeSymbol> type = currentScope.tryLookupType((String) identifierExpression.getValue());
        if (type.isPresent()) {
            throw new UnsupportedOperationException("Custom types are not yet supported");
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
            errors.add(BindingError.raiseConstReassignmentError(variable, assignmentExpression.getSpan()));
            return new BoundAssignmentExpression(variable, variable.getGuard(), initialiser);
        }

        if (!variable.getType().isAssignableFrom(initialiser.getType())) {
            throw new TypeMismatchException(variable.getType(), initialiser.getType());
        }

        return new BoundAssignmentExpression(variable, variable.getGuard(), initialiser);
    }

    private BoundExpression bindStructDeclarationExpression(StructDeclarationExpression structDeclarationExpression) {

        IdentifierExpression identifier = structDeclarationExpression.getIdentifier();

        currentScope = new BoundScope(currentScope);

        List<BoundExpression> members = new ArrayList<>();
        for (Expression member : structDeclarationExpression.getMembers()) {
            members.add(bind(member));
        }

        LinkedHashMap<String, FunctionSymbol> definedFunctions = currentScope.getDefinedFunctions();
        LinkedHashMap<String, VariableSymbol> definedVariables = currentScope.getDefinedVariables();

        //TODO: The defined functions and variables have no knowledge of their owner, two types with the same method may cause issues
        TypeSymbol type = new TypeSymbol((String) identifier.getValue(), definedFunctions, definedVariables);

        //Define constructor as a function
        List<BoundFunctionArgumentExpression> args = new ArrayList<>();
        for (Expression member : structDeclarationExpression.getMembers()) {
            IdentifierExpression memberIdentifier = ((VariableDeclarationExpression) member).getIdentifier();

            VariableSymbol variable = currentScope.tryLookupVariable((String) memberIdentifier.getValue())
                    .orElseThrow(() -> new IllegalStateException("Only variable members allowed in structs"));

            args.add(new BoundFunctionArgumentExpression(variable, variable.getGuard()));
        }

        FunctionSymbol constructor = new FunctionSymbol((String) identifier.getValue(), type, args, null);

        //Generate constructor
        List<BoundExpression> variableExpressions = args.stream()
                .map(BoundFunctionArgumentExpression::getArgument)
                .map(BoundVariableExpression::new)
                .collect(Collectors.toList());

        BoundFunctionDeclarationExpression constructorExpression = new BoundFunctionDeclarationExpression(constructor, args,
                new BoundBlockExpression(new BoundReturnExpression(new BoundStructLiteralExpression(type, variableExpressions)))
        );

        currentScope = currentScope.getParentScope();

        currentScope.declareFunction((String) identifier.getValue(), constructor);

        try {
            currentScope.declareType((String) identifier.getValue(), type);
        } catch (TypeAlreadyDeclaredException tade) {
            errors.add(BindingError.raiseTypeAlreadyDeclared((String) identifier.getValue(), identifier.getSpan()));
        }

        return new BoundBlockExpression(constructorExpression, new BoundStructDeclarationExpression(type, members));
    }

    private BoundExpression bindFunctionDeclarationExpression(FunctionDeclarationExpression functionDeclarationExpression) {

        IdentifierExpression identifier = functionDeclarationExpression.getIdentifier();

        TypeSymbol type = parseType(functionDeclarationExpression.getTypeExpression());

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
            errors.add(BindingError.raiseFunctionAlreadyDeclared((String) identifier.getValue(), identifier.getSpan()));
        }

        BoundBlockExpression body = bindBlockExpression(functionDeclarationExpression.getBody());

        if (functionSymbol.getType() != TypeSymbol.VOID) {
            analyzeBody(functionSymbol, body.getExpressions().iterator());
        }

        currentScope = currentScope.getParentScope();

        return new BoundFunctionDeclarationExpression(functionSymbol, arguments, body);
    }

    private BoundLambdaExpression bindLambdaExpression(LambdaExpression lambdaExpression) {

        //IdentifierExpression identifier = functionDeclarationExpression.getIdentifier();

        currentScope = new BoundScope(currentScope);

        //Declare the arguments within the function's scope
        List<BoundFunctionArgumentExpression> boundArguments = new ArrayList<>();
        for (FunctionArgumentExpression argumentExpression : lambdaExpression.getArgumentExpressions()) {
            boundArguments.add(bindFunctionArgumentExpression(argumentExpression));
        }

        BoundExpression boundBody = new BoundBlockExpression(
                new BoundReturnExpression(bind(lambdaExpression.getExpression()))
        );
        TypeSymbol type = boundBody.getType();

        String anonymousFunctionIdentifier = "lambda-function-" + UUID.randomUUID().toString();

        FunctionSymbol functionSymbol = new FunctionSymbol(anonymousFunctionIdentifier, type, boundArguments, null);
        try {
            //Declare the function in the parent scope
            currentScope.getParentScope().declareFunction(anonymousFunctionIdentifier, functionSymbol);
        } catch (FunctionAlreadyDeclaredException fade) {
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable(anonymousFunctionIdentifier).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, lambdaExpression.getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
        }

        currentScope = currentScope.getParentScope();

        return new BoundLambdaExpression(boundArguments, boundBody);
    }

    private void analyzeBody(FunctionSymbol function, Iterator<BoundExpression> expressions) {

        //BFS the body to ensure all paths return a value of the right type
        for (Iterator<BoundExpression> it = expressions; it.hasNext(); ) {
            BoundExpression expression = it.next();
            if (expression == null) {
                return;
            }
            if (expression.getBoundExpressionType() == BoundExpressionType.RETURN) {
                if (!expression.getType().isAssignableFrom(function.getType())) {
                    throw new TypeMismatchException(function.getType(), expression.getType());
                }
                return;
            }
        }
    }

    private BoundFunctionArgumentExpression bindFunctionArgumentExpression(FunctionArgumentExpression argumentExpression) {

        IdentifierExpression identifier = argumentExpression.getIdentifier();
        TypeSymbol type = parseType(argumentExpression.getTypeExpression());

        //Create placeholder
        try {
            currentScope.declareVariable((String) identifier.getValue(), new VariableSymbol((String) identifier.getValue(), type, null, false, argumentExpression));
        } catch (VariableAlreadyDeclaredException vade) {
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable((String) identifier.getValue()).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, identifier.getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
        }

        BoundExpression guard = null;
        if (argumentExpression.getGuard() != null) {
            guard = bind(argumentExpression.getGuard());
        }

        VariableSymbol argument = buildVariableSymbol(type, identifier, guard, argumentExpression.getConstKeyword() != null, argumentExpression);

        currentScope.reassignVariable((String) identifier.getValue(), argument);

        return new BoundFunctionArgumentExpression(argument, guard);
    }

    private BoundExpression bindFunctionCallExpression(FunctionCallExpression functionCallExpression) {

        List<BoundExpression> boundArguments = new ArrayList<>();
        List<Expression> functionCallExpressionArguments = functionCallExpression.getArguments();
        for (int i = 0; i < functionCallExpressionArguments.size(); i++) {
            BoundExpression boundArgument = bind(functionCallExpressionArguments.get(i));
            boundArguments.add(boundArgument);
        }

        IdentifierExpression identifier = functionCallExpression.getIdentifier();
        Optional<FunctionSymbol> scopedFunction = currentScope.tryLookupFunction((String) identifier.getValue());
        if (scopedFunction.isEmpty()) {
            errors.add(BindingError.raiseUnknownFunction((String) identifier.getValue(), boundArguments, functionCallExpression.getSpan()));
            return new BoundNoOpExpression(); //TODO: Add BoundExceptionExpression
        }
        FunctionSymbol function = scopedFunction.get();

        if (function.getArguments().size() != functionCallExpression.getArguments().size()) {
            errors.add(BindingError.raiseUnknownFunction((String) identifier.getValue(), boundArguments, functionCallExpression.getSpan()));
            return new BoundFunctionCallExpression(function, boundArguments); //TODO: Add BoundExceptionExpression
        }

        List<BoundFunctionArgumentExpression> arguments = function.getArguments();
        for (int i = 0; i < functionCallExpressionArguments.size(); i++) {
            BoundExpression boundArgument = boundArguments.get(i);
            if (!arguments.get(i).getType().isAssignableFrom(boundArgument.getType())) {
                errors.add(BindingError.raiseTypeMismatch(arguments.get(i).getType(), boundArgument.getType(), functionCallExpressionArguments.get(i).getSpan()));
            }
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
            TypeSymbol type = parseType(variableDeclarationExpression.getTypeExpression());
            currentScope.declareVariable((String) identifier.getValue(), new VariableSymbol((String) identifier.getValue(), type, null, false, variableDeclarationExpression));
        } catch (VariableAlreadyDeclaredException vade) {
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable((String) identifier.getValue()).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, identifier.getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
        }

        BoundExpression guard = null;
        if (variableDeclarationExpression.getGuard() != null) {
            guard = bind(variableDeclarationExpression.getGuard());
            assert guard.getType().isAssignableFrom(TypeSymbol.BOOL);
        }

        TypeSymbol type = parseType(variableDeclarationExpression.getTypeExpression());

        if (initialiser != null && !type.isAssignableFrom(initialiser.getType())) {
            errors.add(BindingError.raiseTypeMismatch(type, initialiser.getType(), variableDeclarationExpression.getInitialiser().getSpan()));
        }

        if (type == TypeSymbol.FUNCTION) {
            //We're defining a lambda
            if (!(variableDeclarationExpression.getInitialiser() instanceof LambdaExpression)) {
                throw new IllegalStateException("Functions can not be initialised by " + variableDeclarationExpression.getInitialiser().getClass());
            }
            BoundLambdaExpression lambdaExpression = bindLambdaExpression((LambdaExpression) variableDeclarationExpression.getInitialiser());

            //TODO: guarded lambdas
            FunctionSymbol function = new FunctionSymbol((String) variableDeclarationExpression.getIdentifier().getValue(), lambdaExpression.getBody().getType(), lambdaExpression.getArguments(), null);
            currentScope.declareFunction(function.getName(), function);
            return new BoundFunctionDeclarationExpression(function, lambdaExpression.getArguments(), new BoundBlockExpression(
                    new BoundReturnExpression(lambdaExpression.getBody())
            ));
        }

        VariableSymbol variable = buildVariableSymbol(type, identifier, guard, variableDeclarationExpression.getConstKeyword() != null, variableDeclarationExpression);

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
