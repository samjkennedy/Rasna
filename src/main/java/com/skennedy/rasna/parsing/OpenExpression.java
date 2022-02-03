package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class OpenExpression extends Expression {

    private final IdentifierExpression openInstr;
    private final IdentifierExpression openParen;
    private final Expression filename;
    private final IdentifierExpression comma;
    private final Expression mode;
    private final IdentifierExpression closeParen;

    public OpenExpression(IdentifierExpression openInstr, IdentifierExpression openParen, Expression filename, IdentifierExpression comma, Expression mode, IdentifierExpression closeParen) {

        this.openInstr = openInstr;
        this.openParen = openParen;
        this.filename = filename;
        this.comma = comma;
        this.mode = mode;
        this.closeParen = closeParen;
    }

    public IdentifierExpression getOpenInstr() {
        return openInstr;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public Expression getFilename() {
        return filename;
    }

    public IdentifierExpression getComma() {
        return comma;
    }

    public Expression getMode() {
        return mode;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.OPEN_INTR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)openInstr, openParen, filename, comma, mode, closeParen).iterator();
    }
}
