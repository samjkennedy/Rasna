package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class ImportStatement extends Expression {

    private final IdentifierExpression importKeyword;
    private final IdentifierExpression filePath;
    private final IdentifierExpression asKeyword;
    private final IdentifierExpression name;

    public ImportStatement(IdentifierExpression importKeyword, IdentifierExpression filePath, IdentifierExpression asKeyword, IdentifierExpression name) {

        this.importKeyword = importKeyword;
        this.filePath = filePath;
        this.asKeyword = asKeyword;
        this.name = name;
    }

    public IdentifierExpression getImportKeyword() {
        return importKeyword;
    }

    public IdentifierExpression getFilePath() {
        return filePath;
    }

    public IdentifierExpression getAsKeyword() {
        return asKeyword;
    }

    public IdentifierExpression getName() {
        return name;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.IMPORT;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Stream.of((SyntaxNode)importKeyword, filePath, asKeyword, name)
                .filter(Objects::nonNull)
                .iterator();
    }
}
