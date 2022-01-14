package codes.blitz.game.message.game;

import java.util.List;

public record Unit(String id, String teamId, Position position, List<Position> path, boolean hasDiamond, String diamondId,
        boolean hasSpawned, boolean isSummoning, UnitState lastState) {
}
