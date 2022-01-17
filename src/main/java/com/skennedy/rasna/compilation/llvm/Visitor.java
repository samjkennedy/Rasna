package com.skennedy.rasna.compilation.llvm;

import com.skennedy.rasna.typebinding.BoundExpression;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public abstract class Visitor<E extends BoundExpression> {

    public abstract LLVMValueRef visit(E expression, Scope scope);
}
