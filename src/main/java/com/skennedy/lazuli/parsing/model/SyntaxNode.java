package com.skennedy.lazuli.parsing.model;

import com.skennedy.lazuli.diagnostics.TextSpan;

import java.util.Iterator;

public interface SyntaxNode {

    TextSpan getSpan();

    ExpressionType getExpressionType();

    Iterator<SyntaxNode> getChildren();
}
