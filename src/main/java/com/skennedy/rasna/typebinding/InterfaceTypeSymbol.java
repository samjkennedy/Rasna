package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class InterfaceTypeSymbol extends TypeSymbol {

    //TODO: tie this to imports
    public static final InterfaceTypeSymbol CLOSABLE = new InterfaceTypeSymbol("Closable", Collections.singletonList(new BoundFunctionSignatureExpression("close", Collections.emptyList(), BOOL)));

    private final List<BoundFunctionSignatureExpression> signatures;

    public InterfaceTypeSymbol(String name, List<BoundFunctionSignatureExpression> signatures) {
        super(name, new LinkedHashMap<>());
        this.signatures = signatures;
    }

    public List<BoundFunctionSignatureExpression> getSignatures() {
        return signatures;
    }



    public static List<InterfaceTypeSymbol> getBuiltinInterfaces() {
        return Arrays.asList(CLOSABLE);
    }
}
