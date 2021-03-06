package com.skennedy.rasna.parsing.model;

import com.skennedy.rasna.diagnostics.TextSpan;
import com.skennedy.rasna.parsing.Expression;

import java.util.Iterator;

public interface SyntaxNode {

    TextSpan getSpan();

    ExpressionType getExpressionType();

    Iterator<SyntaxNode> getChildren();
}
