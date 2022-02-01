package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;
import java.util.List;

public class InterfaceTypeSymbol extends TypeSymbol {

    private final List<BoundFunctionSignatureExpression> functions;

    public InterfaceTypeSymbol(String name, List<BoundFunctionSignatureExpression> functions) {
        super(name, new LinkedHashMap<>());
        this.functions = functions;
    }
}
