package battleship;

import battleship.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static battleship.Game.FIELD_SIZE;
import static battleship.Game.ROW_FIRST_INDICATOR;

public class BattleField {

    private static final Coordinates TOP = new Coordinates(0, -1);
    private static final Coordinates BOTTOM = new Coordinates(0, 1);
    private static final Coordinates LEFT = new Coordinates(-1, 0);
    private static final Coordinates RIGHT = new Coordinates(1, 0);

    private final CellType[][] field;
    private final List<ShipCoordinates> ships = new ArrayList<>();

    private BattleField(CellType[][] field) {
        this.field = field;
    }

    public static BattleField create() {
        CellType[][] result = new CellType[FIELD_SIZE][FIELD_SIZE];
        for (CellType[] row : result) {
            Arrays.fill(row, CellType.EMPTY);
        }
        return new BattleField(result);
    }

    public void print(PrintingMode mode) {
        System.out.print(" ");
        for (int i = 1; i <= FIELD_SIZE; i++) {
            System.out.printf(" %d", i);
        }
        System.out.println();
        for (int i = 0; i < field.length; i++) {
            CellType[] row = field[i];
            System.out.printf("%c", ROW_FIRST_INDICATOR + i);
            for (CellType cell : row) {
                System.out.printf(" %c", cellToChar(cell, mode));
            }
            System.out.println();
        }
    }

    private char cellToChar(CellType cell, PrintingMode mode) {
        return switch (cell) {
            case HIT, MISS, EMPTY -> cell.getChar();
            case SHIP -> mode == PrintingMode.NORMAL ? cell.getChar() : CellType.EMPTY.getChar();
        };
    }

    public void addShip(Ship ship, ShipCoordinates coordinates) {
        checkCoordinatesAreCorrect(coordinates);
        coordinates = rearrangeShipCoordinates(coordinates);
        Coordinates first = coordinates.first();
        Coordinates second = coordinates.second();
        int distance = getDistance(first, second);
        if (ship.getSize() != distance) {
            throw new IllegalArgumentException("Wrong length of the %s!".formatted(ship.getName()));
        }
        if (first.x() == second.x()) {
            int top = first.y();
            int bottom = second.y();
            for (int y = top; y <= bottom; y++) {
                checkIsNotCollide(new Coordinates(first.x(), y));
            }
            for (int y = top; y <= bottom; y++) {
                setValue(new Coordinates(first.x(), y), CellType.SHIP);
            }
        } else {
            int left = first.x();
            int right = second.x();
            for (int x = left; x <= right; x++) {
                checkIsNotCollide(new Coordinates(x, first.y()));
            }
            for (int x = left; x <= right; x++) {
                setValue(new Coordinates(x, first.y()), CellType.SHIP);
            }
        }
        ships.add(coordinates);
    }

    private void checkCoordinatesAreCorrect(ShipCoordinates coordinates) {
        Coordinates f = coordinates.first();
        Coordinates s = coordinates.second();
        if (f.x() != s.x() && f.y() != f.y()) {
            throw new IllegalStateException("Wrong ship coordinates!");
        }
    }

    private ShipCoordinates rearrangeShipCoordinates(ShipCoordinates coordinates) {
        Coordinates first = coordinates.first();
        Coordinates second = coordinates.second();
        if (first.x() == second.x()) {
            if (first.y() > second.y()) {
                return new ShipCoordinates(second, first);
            }
        } else {
            if (first.x() > second.x()) {
                return new ShipCoordinates(second, first);
            }
        }
        return coordinates;
    }

    private int getDistance(Coordinates first, Coordinates second) {
        int result;
        if (first.x() == second.x()) {
            result = second.y() - first.y();
        } else {
            result = second.x() - first.x();
        }
        return result + 1;
    }

    private CellType getValue(Coordinates coordinates) {
        return field[coordinates.y()][coordinates.x()];
    }

    private void setValue(Coordinates coordinates, CellType cellType) {
        field[coordinates.y()][coordinates.x()] = cellType;
    }

    private boolean isTypeOfCell(Coordinates coordinates, CellType type) {
        return field[coordinates.y()][coordinates.x()] == type;
    }

    private boolean isValidCoordinates(Coordinates coordinates) {
        return coordinates.x() >= 0 && coordinates.x() < FIELD_SIZE
                && coordinates.y() >= 0 && coordinates.y() < FIELD_SIZE;
    }

    private void checkIsNotCollide(Coordinates coordinates) {
        Coordinates[] toCheck = {
                coordinates,
                coordinates.plus(TOP),
                coordinates.plus(BOTTOM),
                coordinates.plus(LEFT),
                coordinates.plus(RIGHT),
        };
        for (Coordinates c : toCheck) {
            if (isValidCoordinates(c) && !isTypeOfCell(c, CellType.EMPTY)) {
                throw new IllegalStateException("Wrong ship location!");
            }
        }
    }

    public ShotResult shot(Coordinates shot) {
        if (!isValidCoordinates(shot)) {
            throw new IllegalStateException("Wrong shot coordinates!");
        }
        return getShotResult(shot);
    }

    private ShotResult getShotResult(Coordinates shot) {
        if (isTypeOfCell(shot, CellType.HIT)) {
            return ShotResult.HIT;
        }
        ShotResult shotResult;
        if (isTypeOfCell(shot, CellType.SHIP)) {
            setValue(shot, CellType.HIT);
            shotResult = ShotResult.HIT;
            ShipCoordinates ship = findShip(shot);
            if (isShipSunk(ship)) {
                shotResult = ShotResult.SHIP_SUNK;
                if (isAllShipsSunk()) {
                    shotResult = ShotResult.LAST_SHIP_SUNK;
                }
            }
        } else {
            setValue(shot, CellType.MISS);
            shotResult = ShotResult.MISS;
        }
        return shotResult;
    }

    private ShipCoordinates findShip(Coordinates coordinates) {
        for (ShipCoordinates ship : ships) {
            Coordinates f = ship.first();
            Coordinates s = ship.second();
            if (f.x() <= coordinates.x() && coordinates.x() <= s.x()
                    && f.y() <= coordinates.y() && coordinates.y() <= s.y()) {
                return ship;
            }
        }
        throw new IllegalStateException("Ship not found!");
    }

    private boolean isShipSunk(ShipCoordinates ship) {
        return ship.coordinates().stream().map(this::getValue).allMatch(c -> c == CellType.HIT);
    }

    private boolean isAllShipsSunk() {
        for (ShipCoordinates ship : ships) {
            if (!isShipSunk(ship)) {
                return false;
            }
        }
        return true;
    }
}
