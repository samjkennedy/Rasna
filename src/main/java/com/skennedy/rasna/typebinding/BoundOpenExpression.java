package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundOpenExpression implements BoundExpression {

    private final BoundExpression filename;
    private final BoundExpression mode;

    public BoundOpenExpression(BoundExpression filename, BoundExpression mode) {
        this.filename = filename;
        this.mode = mode;
    }

    public BoundExpression getFilename() {
        return filename;
    }

    public BoundExpression getMode() {
        return mode;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.OPEN_INTRINSIC;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.FILE;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(filename, mode).iterator();
    }
}
