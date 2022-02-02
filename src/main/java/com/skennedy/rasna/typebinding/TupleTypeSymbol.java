package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TupleTypeSymbol extends TypeSymbol {

    private final List<TypeSymbol> types;

    public TupleTypeSymbol(List<TypeSymbol> types) {
        super(buildName(types), new LinkedHashMap<>(new LinkedHashMap<>(Map.of(
                "len", new VariableSymbol("len", INT, null, true, null)
        ))));
        this.types = types;
    }

    public List<TypeSymbol> getTypes() {
        return types;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isAssignableFrom(TypeSymbol other) {
        boolean assignable = super.isAssignableFrom(other);

        if (other instanceof TupleTypeSymbol) {
            return isAssignableFrom((TupleTypeSymbol)other);
        }
        return assignable;
    }

    public boolean isAssignableFrom(TupleTypeSymbol other) {
        if (types.size() != other.getTypes().size()) {
            return false;
        }

        for (int i = 0; i < types.size(); i++) {
            if (!types.get(i).isAssignableFrom(other.getTypes().get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TupleTypeSymbol)) {
            return false;
        }
        TupleTypeSymbol other = (TupleTypeSymbol) o;
        if (types.size() != other.getTypes().size()) {
            return false;
        }

        for (int i = 0; i < types.size(); i++) {
            if (!types.get(i).isAssignableFrom(other.getTypes().get(i))) {
                return false;
            }
        }
        return true;
    }

    private static String buildName(List<TypeSymbol> types) {
        return "(" + types.stream()
                .map(TypeSymbol::toString)
                .collect(Collectors.joining(", ")) + ")";
    }
}
