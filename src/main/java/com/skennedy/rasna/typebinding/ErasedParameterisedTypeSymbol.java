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
        this.erasures = erasures;
        this.fields =  eraseFieldTypes(fields, erasures);
    }

    private static LinkedHashMap<String, VariableSymbol> eraseFieldTypes(LinkedHashMap<String, VariableSymbol> fields, Map<String, TypeSymbol> erasures) {
        for (Map.Entry<String, VariableSymbol> field : fields.entrySet()) {
            VariableSymbol variable = field.getValue();
            TypeSymbol type = variable.getType();
            if (erasures.containsKey(type.getName())) {

                TypeSymbol erasedType = erasures.get(type.getName());
                if (type instanceof ArrayTypeSymbol) {
                    erasedType = new ArrayTypeSymbol(erasedType);
                }

                VariableSymbol erasedVariable = new VariableSymbol(variable.getName(), erasedType, variable.getGuard(), variable.isReadOnly(), variable.getDeclaration());
                fields.replace(variable.getName(), variable, erasedVariable);
            }
        }
        return fields;
    }

    @Override
    public String getName() {
        return toString();
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
        return name + "<" + erasures.values().stream().map(TypeSymbol::toString).collect(Collectors.joining(", ")) + ">";
    }
}
