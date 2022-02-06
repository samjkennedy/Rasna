package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;
import java.util.List;

public class InterfaceTypeSymbol extends TypeSymbol {

    private final List<BoundFunctionSignatureExpression> signatures;

    public InterfaceTypeSymbol(String name, List<BoundFunctionSignatureExpression> signatures) {
        super(name, new LinkedHashMap<>());
        this.signatures = signatures;
    }

    public List<BoundFunctionSignatureExpression> getSignatures() {
        return signatures;
    }
}
