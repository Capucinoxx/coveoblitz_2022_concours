package codes.blitz.game.message.game;

import java.util.List;

public record Team(String id, String name, int score, List<Unit> units, List<String> errors) {
}
