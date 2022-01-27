package battleship.model;

public record Coordinates(int x, int y) {
    public Coordinates plus(Coordinates other) {
        return new Coordinates(x + other.x, y + other.y);
    }
}
