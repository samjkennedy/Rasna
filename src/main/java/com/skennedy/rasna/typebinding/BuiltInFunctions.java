package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BuiltInFunctions {

    //Files
    public static FunctionSymbol READ_CHAR = new FunctionSymbol("readChar", TypeSymbol.CHAR, Collections.singletonList(buildArg("file", TypeSymbol.FILE)), null);
    public static FunctionSymbol WRITE_CHAR = new FunctionSymbol("writeChar", TypeSymbol.UNIT, Arrays.asList(buildArg("file", TypeSymbol.FILE), buildArg("c", TypeSymbol.CHAR)), null);

    private static BoundFunctionParameterExpression buildArg(String name, TypeSymbol type) {
        return new BoundFunctionParameterExpression(false, new VariableSymbol(name, type, null, false, null), null);
    }

    public static List<FunctionSymbol> getBuiltinFunctions() {
        return Arrays.asList(READ_CHAR, WRITE_CHAR);
    }
}
