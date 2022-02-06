package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;

public class GenericTypeSymbol extends TypeSymbol {
    public GenericTypeSymbol(String name, LinkedHashMap<String, VariableSymbol> fields) {
        super(name, fields);
    }
}
