package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.skennedy.rasna.typebinding.SymbolType.VARIABLE;

public class ArrayTypeSymbol extends TypeSymbol {

    private final TypeSymbol type;

    public ArrayTypeSymbol(TypeSymbol type) {
        super(type.getName(), new LinkedHashMap<>(Map.of("len", new VariableSymbol("len", INT, null, true, null))));
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArrayTypeSymbol)) {
            return false;
        }
        ArrayTypeSymbol that = (ArrayTypeSymbol) o;
        return getType() == that.getType();
    }

    @Override
    public boolean isAssignableFrom(TypeSymbol other) {
        boolean isAssignable = super.isAssignableFrom(other);

        if (isAssignable) {
            return true;
        }

        return other == STRING && type == CHAR;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getType());
    }

    public TypeSymbol getType() {
        return type;
    }

    @Override
    public SymbolType getSymbolType() {
        return VARIABLE;
    }

    @Override
    public String toString() {
        return getName() + "[]";
    }
}
