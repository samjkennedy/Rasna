package com.skennedy.rasna.typebinding;

import com.skennedy.rasna.diagnostics.TextSpan;

public class BindingWarning {

    private final String message;
    private final TextSpan span;

    public BindingWarning(String message, TextSpan span) {

        this.message = message;
        this.span = span;
    }

    public String getMessage() {
        return message;
    }

    public TextSpan getSpan() {
        return span;
    }

    public static BindingWarning raiseConditionAlwaysTrue(TextSpan span) {

        return new BindingWarning("Condition is always true", span);
    }

    public static BindingWarning raiseConditionAlwaysFalse(TextSpan span) {

        return new BindingWarning("Condition is always false", span);
    }
}
