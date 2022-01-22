package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;

public class EnumTypeSymbol extends TypeSymbol {

    private LinkedHashMap<String, VariableSymbol> members;

    public EnumTypeSymbol(String name, LinkedHashMap<String, VariableSymbol> members) {
        super(name, members);
        this.members = members;
    }

    public int ordinalOf(String member) {
        int idx = 0;
        for (String name : members.keySet()) {
            if (name.equals(member)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }
}
