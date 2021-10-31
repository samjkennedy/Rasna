package com.skennedy.lazuli.parsing.model;

import java.util.Iterator;

public interface SyntaxNode {

    ExpressionType getExpressionType();

    Iterator<SyntaxNode> getChildren();
}
