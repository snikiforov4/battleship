package battleship.model;

import java.util.ArrayList;
import java.util.List;

public record ShipCoordinates(Coordinates first, Coordinates second) {

    public List<Coordinates> coordinates() {
        List<Coordinates> result = new ArrayList<>();
        if (first.x() == second.x()) {
            for (int y = first.y(); y <= second.y(); y++) {
                result.add(new Coordinates(first.x(), y));
            }
        } else {
            for (int x = first.x(); x <= second.x(); x++) {
                result.add(new Coordinates(x, first.y()));
            }
        }
        return result;
    }
}
