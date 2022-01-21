package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class TypeExpression extends Expression {

    private IdentifierExpression colon;
    private final Expression identifier;
    private final IdentifierExpression openSquareBracket;
    private final IdentifierExpression closeSquareBracket;

    public TypeExpression(IdentifierExpression colon, Expression typeExpression, IdentifierExpression openSquareBracket, IdentifierExpression closeSquareBracket) {
        this.colon = colon;
        this.identifier = typeExpression;
        this.openSquareBracket = openSquareBracket;
        this.closeSquareBracket = closeSquareBracket;
    }

    public IdentifierExpression getColon() {
        return colon;
    }

    public Expression getIdentifier() {
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
