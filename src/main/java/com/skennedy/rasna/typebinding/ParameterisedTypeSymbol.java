package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;
import java.util.List;

public class ParameterisedTypeSymbol extends TypeSymbol {

    private final List<String> genericParameters;

    public ParameterisedTypeSymbol(String name, LinkedHashMap<String, VariableSymbol> definedVariabled, List<String> genericParameters) {
        super(name, definedVariabled);
        this.genericParameters = genericParameters;
    }

    public List<String> getGenericParameters() {
        return genericParameters;
    }

    @Override
    public String toString() {
        return getName() + "<" + String.join(", ", genericParameters) + ">";
    }

}
