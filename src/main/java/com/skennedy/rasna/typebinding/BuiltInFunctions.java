package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//TODO: Have an opt in system, like `import io.rasna`
public class BuiltInFunctions {

    //IO
    public static FunctionSymbol READ_IN = new FunctionSymbol("readIn", TypeSymbol.CHAR, Collections.emptyList(), null);
    //Files
    public static FunctionSymbol OPEN_R = new FunctionSymbol("open", TypeSymbol.FILE, Collections.singletonList(buildArg("filename", TypeSymbol.STRING)), null);
    public static FunctionSymbol OPEN = new FunctionSymbol("open", TypeSymbol.FILE, Arrays.asList(buildArg("filename", TypeSymbol.STRING), buildArg("mode", TypeSymbol.STRING)), null);
    public static FunctionSymbol READ_CHAR = new FunctionSymbol("readChar", TypeSymbol.CHAR, Collections.singletonList(buildArg("file", TypeSymbol.FILE)), null);
    public static FunctionSymbol WRITE_CHAR = new FunctionSymbol("write", TypeSymbol.UNIT, Arrays.asList(buildArg("file", TypeSymbol.FILE), buildArg("c", TypeSymbol.CHAR)), null);
    public static FunctionSymbol CLOSE = new FunctionSymbol("close", TypeSymbol.BOOL, Collections.singletonList(buildArg("file", TypeSymbol.FILE)), null);

    private static BoundFunctionParameterExpression buildArg(String name, TypeSymbol type) {
        return new BoundFunctionParameterExpression(false, new VariableSymbol(name, type, null, false, null), null);
    }

    public static List<FunctionSymbol> getBuiltinFunctions() {
        return Arrays.asList(READ_IN, OPEN, OPEN_R, READ_CHAR, WRITE_CHAR, CLOSE);
    }
}
