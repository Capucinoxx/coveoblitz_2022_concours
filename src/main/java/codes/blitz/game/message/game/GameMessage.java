package codes.blitz.game.message.game;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GameMessage(int tick, int totalTick, List<Team> teams, GameMap map, String teamId,
        Map<String, Team> teamsMapById, GameConfig gameConfig, Map<Integer, List<String>> teamPlayOrderings) {
    @JsonCreator
    public GameMessage(@JsonProperty("tick") int tick,
                       @JsonProperty("totalTick") int totalTick,
                       @JsonProperty("teams") List<Team> teams,
                       @JsonProperty("map") GameMap map,
                       @JsonProperty("teamId") String teamId,
                       @JsonProperty("gameConfig") GameConfig gameConfig,
                       @JsonProperty("teamPlayOrderings") Map<Integer, List<String>> teamPlayOrderings)
    {
        this(tick,
             totalTick,
             teams,
             map,
             teamId,
             teams.stream().collect(Collectors.toMap(Team::id, Function.identity())),
             gameConfig,
             teamPlayOrderings);
    }
}
