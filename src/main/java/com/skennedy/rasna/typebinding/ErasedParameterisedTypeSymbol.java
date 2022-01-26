package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ErasedParameterisedTypeSymbol extends TypeSymbol {

    private final String name;
    private final LinkedHashMap<String, VariableSymbol> fields;
    private final Map<String, TypeSymbol> erasures;

    public ErasedParameterisedTypeSymbol(String name, LinkedHashMap<String, VariableSymbol> fields, Map<String, TypeSymbol> erasures) {
        super(name, fields);
        this.name = name;
        this.fields = fields;
        this.erasures = erasures;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LinkedHashMap<String, VariableSymbol> getFields() {
        return fields;
    }

    public Map<String, TypeSymbol> getErasures() {
        return erasures;
    }

    @Override
    public String toString() {
        return getName() + "<" + erasures.values().stream().map(TypeSymbol::toString).collect(Collectors.joining(", ")) + ">";
    }
}
