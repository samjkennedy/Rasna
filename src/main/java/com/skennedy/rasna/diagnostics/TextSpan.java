package com.skennedy.rasna.diagnostics;

import com.skennedy.rasna.lexing.model.Location;

public final class TextSpan {

    private final Location start;
    private final Location end;

    public TextSpan(Location start, Location end) {

        this.start = start;
        this.end = end;
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return start + " to " + end;
    }
}
