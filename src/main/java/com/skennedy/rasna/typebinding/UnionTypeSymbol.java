package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UnionTypeSymbol extends TypeSymbol {

    private List<TypeSymbol> types;

    public UnionTypeSymbol(List<TypeSymbol> types) {
        super(buildName(types), new LinkedHashMap<>());
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
        if (other instanceof UnionTypeSymbol) {
            return isAssignableFrom((UnionTypeSymbol) other);
        }
        for (TypeSymbol type : types) {
            if (type.isAssignableFrom(other)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAssignableFrom(UnionTypeSymbol other) {
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
                .collect(Collectors.joining("|")) + ")";
    }
}
