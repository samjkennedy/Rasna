package com.skennedy.rasna.parsing;

import com.skennedy.rasna.diagnostics.TextSpan;
import com.skennedy.rasna.lexing.model.Location;
import com.skennedy.rasna.lexing.model.Token;
import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Collections;
import java.util.Iterator;

public class LiteralExpression extends Expression {

    private final Token token;
    private final Object value;

    public LiteralExpression(Token token, Object value) {
        this.token = token;
        this.value = value;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.LITERAL_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Collections.emptyIterator();
    }

    public Object getValue() {
        return value;
    }

    @Override
    public TextSpan getSpan() {
        return new TextSpan(token.getLocation(), Location.fromOffset(token.getLocation(), (value.toString()).length() - 1));
    }
}
