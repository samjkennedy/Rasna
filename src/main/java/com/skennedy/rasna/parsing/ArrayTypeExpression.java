package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class ArrayTypeExpression extends TypeExpression {

    private final IdentifierExpression openSquareBracket;
    private final IdentifierExpression closeSquareBracket;

    public ArrayTypeExpression(Expression typeExpression, IdentifierExpression openSquareBracket, IdentifierExpression closeSquareBracket) {
        super(typeExpression);
        this.openSquareBracket = openSquareBracket;
        this.closeSquareBracket = closeSquareBracket;
    }

    public Expression getTypeExpression() {
        return type;
    }

    public IdentifierExpression getOpenSquareBracket() {
        return openSquareBracket;
    }

    public IdentifierExpression getCloseSquareBracket() {
        return closeSquareBracket;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.TYPE_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)type, openSquareBracket, closeSquareBracket).stream()
                .filter(Objects::nonNull)
                .iterator();
    }
}
