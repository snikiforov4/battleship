package battleship;

import battleship.model.*;

import java.io.IOException;
import java.util.Scanner;

public class Game {

    public static final int FIELD_SIZE = 10;
    public static final char ROW_FIRST_INDICATOR = 'A';
    private static final char ROW_LAST_INDICATOR = 'A' + FIELD_SIZE;

    private final BattleField player1BattleField;
    private final BattleField player2BattleField;
    private final Scanner scanner;

    private Game(BattleField player1BattleField,
                 BattleField player2BattleField) {
        this.player1BattleField = player1BattleField;
        this.player2BattleField = player2BattleField;
        this.scanner = new Scanner(System.in);
    }

    public static Game create() {
        return new Game(BattleField.create(), BattleField.create());
    }

    public void play() {
        initPlayer("Player 1", player1BattleField);
        passMoveToAnotherPlayer();
        initPlayer("Player 2", player2BattleField);
        passMoveToAnotherPlayer();

        ShotResult shotResult;
        while (true) {
            shotResult = takeTurn("Player 1", player1BattleField, player2BattleField);
            if (shotResult == ShotResult.LAST_SHIP_SUNK) {
                break;
            }
            passMoveToAnotherPlayer();
            shotResult = takeTurn("Player 2", player2BattleField, player1BattleField);
            if (shotResult == ShotResult.LAST_SHIP_SUNK) {
                break;
            }
            passMoveToAnotherPlayer();
        }
    }

    private void initPlayer(String playerName, BattleField battleField) {
        System.out.printf("%s, place your ships to the game field%n%n", playerName);
        battleField.print(PrintingMode.NORMAL);
        for (Ship ship : Ship.values()) {
            System.out.printf("%nEnter the coordinates of the %s (%d cells): %n",
                    ship.getName(), ship.getSize());
            while (true) {
                try {
                    ShipCoordinates shipCoordinates = readShipCoordinates();
                    battleField.addShip(ship, shipCoordinates);
                    battleField.print(PrintingMode.NORMAL);
                } catch (Exception e) {
                    System.out.printf("%nError! %s Try again:%n", e.getMessage());
                    continue;
                }
                break;
            }
        }
    }

    private ShipCoordinates readShipCoordinates() {
        Coordinates first = toCoordinates(scanner.next());
        Coordinates second = toCoordinates(scanner.next());
        return new ShipCoordinates(first, second);
    }

    private Coordinates toCoordinates(String text) throws CoordinateParsingException {
        if (text.length() < 2 || text.length() > 3) {
            throw new CoordinateParsingException("Invalid coordinate length: %s!".formatted(text));
        }
        int x0 = Integer.parseInt(text.substring(1), 10);
        if (x0 < 1 || x0 > FIELD_SIZE) {
            throw new CoordinateParsingException("Column value invalid: %s!".formatted(text));
        }
        int x = x0 - 1;
        int y0 = text.codePointAt(0);
        if (y0 < ROW_FIRST_INDICATOR || y0 > ROW_LAST_INDICATOR) {
            throw new CoordinateParsingException("Row value invalid: %s!".formatted(text));
        }
        int y = y0 - ROW_FIRST_INDICATOR;
        return new Coordinates(x, y);
    }

    private void passMoveToAnotherPlayer() {
        System.out.printf("%nPress Enter and pass the move to another player%n");
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ShotResult takeTurn(String playerName,
                                BattleField playerBattleField,
                                BattleField opponentBattleField) {
        opponentBattleField.print(PrintingMode.FOG_OF_WAR);
        System.out.println("---------------------");
        playerBattleField.print(PrintingMode.NORMAL);
        System.out.printf("%n%s, it's your turn:%n", playerName);
        ShotResult result;
        do {
            try {
                Coordinates shotCoordinates = toCoordinates(scanner.next());
                result = opponentBattleField.shot(shotCoordinates);
                break;
            } catch (Exception e) {
                System.out.printf("%nError! %s Try again:%n", e.getMessage());
            }
        } while (true);
        switch (result) {
            case HIT -> System.out.printf("%nYou hit a ship!%n");
            case MISS -> System.out.printf("%nYou missed!%n");
            case SHIP_SUNK -> System.out.printf("%nYou sank a ship!%n");
            case LAST_SHIP_SUNK -> System.out.printf("%nYou sank the last ship. You won. Congratulations!");
            default -> throw new RuntimeException("Unknown result: " + result);
        }
        return result;
    }
}
