package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TupleTypeSymbol extends TypeSymbol {

    private final List<TypeSymbol> types;

    public TupleTypeSymbol(List<TypeSymbol> types) {
        super("Tuple", new LinkedHashMap<>());
        this.types = types;
    }

    public List<TypeSymbol> getTypes() {
        return types;
    }

    @Override
    public String toString() {
        return "(" + types.stream()
                .map(TypeSymbol::toString)
                .collect(Collectors.joining(", ")) + ")";
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
}
