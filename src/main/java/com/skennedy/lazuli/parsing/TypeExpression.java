package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class TypeExpression extends Expression {

    private IdentifierExpression colon;
    private final IdentifierExpression identifier;
    private final IdentifierExpression openSquareBracket;
    private final IdentifierExpression closeSquareBracket;

    public TypeExpression(IdentifierExpression colon, IdentifierExpression identifier, IdentifierExpression openSquareBracket, IdentifierExpression closeSquareBracket) {
        this.colon = colon;
        this.identifier = identifier;
        this.openSquareBracket = openSquareBracket;
        this.closeSquareBracket = closeSquareBracket;
    }

    public IdentifierExpression getColon() {
        return colon;
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
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
        return Arrays.asList((SyntaxNode)colon, identifier, openSquareBracket, closeSquareBracket).stream()
                .filter(Objects::nonNull)
                .iterator();
    }
}
