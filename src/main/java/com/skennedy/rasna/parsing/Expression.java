package com.skennedy.rasna.parsing;

import com.skennedy.rasna.diagnostics.TextSpan;
import com.skennedy.rasna.lexing.model.Location;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.List;

public abstract class Expression implements SyntaxNode {

    @Override
    public TextSpan getSpan() {
        List<SyntaxNode> children = new ArrayList<>();
        getChildren().forEachRemaining(child -> {
                if (child != null) {
                    children.add(child);
                }
        });
        Location start = children.get(0).getSpan().getStart();
        Location end = children.get(children.size() - 1).getSpan().getEnd();

        return new TextSpan(start, end);
    }

    public static TextSpan getSpan(List<SyntaxNode> children) {
        Location start = children.get(0).getSpan().getStart();
        Location end = children.get(children.size() - 1).getSpan().getEnd();

        return new TextSpan(start, end);
    }
}
