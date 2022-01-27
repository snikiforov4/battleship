package battleship.model;

public enum CellType {
    EMPTY('~'),
    SHIP('O'),
    HIT('X'),
    MISS('M'),
    ;

    private final char c;

    CellType(char c) {
        this.c = c;
    }

    public char getChar() {
        return c;
    }
}
