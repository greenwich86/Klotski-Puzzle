package model;

public enum Direction {
    LEFT(0, -1), UP(-1, 0), RIGHT(0, 1), DOWN(1, 0),
    ;
    private final int rowOffset;
    private final int colOffset;

    Direction(int rowOffset, int colOffset) {
        this.rowOffset = rowOffset;
        this.colOffset = colOffset;
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public int getColOffset() {
        return colOffset;
    }
}