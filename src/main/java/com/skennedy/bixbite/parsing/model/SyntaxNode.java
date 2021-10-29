package com.skennedy.bixbite.parsing.model;

import java.util.Iterator;

public interface SyntaxNode {

    ExpressionType getExpressionType();

    Iterator<SyntaxNode> getChildren();
}
