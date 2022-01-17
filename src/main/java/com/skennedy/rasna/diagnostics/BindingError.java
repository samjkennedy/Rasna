package com.skennedy.rasna.diagnostics;

import com.skennedy.rasna.lexing.model.Location;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.OpType;
import com.skennedy.rasna.typebinding.BoundExpression;
import com.skennedy.rasna.typebinding.TypeSymbol;
import com.skennedy.rasna.typebinding.VariableSymbol;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BindingError {

    private final String message;
    private final TextSpan span;

    public BindingError(String message, TextSpan span) {

        this.message = message;
        this.span = span;
    }

    public static BindingError raiseTypeMismatch(TypeSymbol expected, TypeSymbol actual, TextSpan span) {

        if (actual == TypeSymbol.STRING) {
            //String spans do not include the surrounding "s
            span = new TextSpan(Location.fromOffset(span.getStart(), -1), Location.fromOffset(span.getEnd(), 1));
        }
        return new BindingError("Expected type `" + expected + "` but got `" + actual + "`:", span);
    }

    //TODO: Have the location the variable already declared
    public static BindingError raiseVariableAlreadyDeclared(VariableSymbol variable, TextSpan span, TextSpan originalDeclarationSpan) {

        return new BindingError("Variable `" + variable.getName() + ": " + variable.getType().getName() + "` is already defined in the scope at " + originalDeclarationSpan + ":", span);
    }

    public static BindingError raiseFunctionAlreadyDeclared(String name, TextSpan span) {

        return new BindingError("Function " + name + " is already defined in the scope:", span);
    }

    public static BindingError raiseTypeAlreadyDeclared(String name, TextSpan span) {

        return new BindingError("Type " + name + " is already defined in the scope:", span);
    }

    public static BindingError raiseUnknownType(String name, TextSpan span) {

        return new BindingError("Type " + name + " is not defined in the scope:", span);
    }

    public static BindingError raiseUnknownNamespace(String name, TextSpan span) {

        return new BindingError("Namespace " + name + " is not defined in the scope:", span);
    }

    public static BindingError raiseUnknownFunction(String function, List<BoundExpression> arguments, TextSpan span) {

        return new BindingError("Function " + function + " is not defined in the scope for arguments (" +
                arguments.stream()
                        .filter(Objects::nonNull)
                        .map(BoundExpression::getType)
                        .filter(Objects::nonNull)
                        .map(TypeSymbol::toString)
                        .collect(Collectors.joining(", ")) + "):", span);
    }

    public static BindingError raiseUnknownMember(String member, TypeSymbol type, TextSpan span) {

        return new BindingError("No such member " + member + " for type " + type.getName(), span);
    }

    public static BindingError raiseConstReassignmentError(VariableSymbol variable, TextSpan span) {

        return new BindingError("Value `" + variable.getName() + ": " + variable.getType().getName() + "` is constant and cannot be reassigned:", span);
    }

    public static BindingError raiseInvalidOperationException(OpType operation, TypeSymbol leftType, TypeSymbol rightType, TextSpan span) {

        return new BindingError("Operation `" + operation.getName() + "` is not defined for types `" + leftType + "` and `" + rightType + "`:", span);
    }

    public static BindingError raise(String message, TextSpan span) {

        return new BindingError(message + ":", span);
    }

    public static BindingError raiseInvalidOperationException(OpType operator, TypeSymbol operandType, TextSpan span) {

        return new BindingError("Operation `" + operator.getName() + "` is not defined for type `" + operandType + "`:", span);
    }

    public static BindingError raiseNonConstAssignmentError(IdentifierExpression identifier, TextSpan span) {

        return new BindingError("Cannot assign non constant value to constant `" + identifier + "`:", span);
    }

    public static BindingError raiseMissingReturnExpression(TextSpan span) {

        return new BindingError("Missing return statement:", span);
    }

    public static BindingError raiseUnknownIdentifier(String identifier, TextSpan span) {

        return new BindingError("Unknown identifier `" + identifier + "`:", span);
    }

    public static BindingError raiseUnreachableExpression(TextSpan span) {

        return new BindingError("Unreachable expression:", span);
    }

    public String getMessage() {
        return message;
    }

    public TextSpan getSpan() {
        return span;
    }
}
