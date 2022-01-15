package codes.blitz.game.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import codes.blitz.game.message.game.GameMap;
import codes.blitz.game.message.game.GameMessage;
import codes.blitz.game.message.game.Position;
import codes.blitz.game.message.game.Team;
import codes.blitz.game.message.game.TileType;
import codes.blitz.game.message.game.UnitAction;
import codes.blitz.game.message.game.UnitActionType;
import jdk.jshell.execution.Util;

public class Bot
{
    public static final Integer taha_fait_vraiment_chier = 5;

    public Bot()
    {
        System.out.println("Initializing your super duper mega bot.");
        // initialize some variables you will need throughout the game here
    }

    /*
     * Here is where the magic happens, for now the moves are random. I bet you can do better ;)
     *
     * No path finding is required, you can simply send a destination per unit and the game will move your unit towards
     * it in the next turns.
     */
    public List<UnitAction> getNextActions(GameMessage gameMessage)
    {
        Utils.createDiamondMap();
        Utils.createUnitMap();

        Team myTeam = gameMessage.teamsMapById().get(gameMessage.teamId());
        GameMap map = gameMessage.map();

        var deadUnitsActions = myTeam.units()
                                     .stream()
                                     .filter(unit -> !unit.hasSpawned())
                                     .map(unit -> new UnitAction(UnitActionType.SPAWN,
                                                                 unit.id(),
                                                                 findRandomSpawn(map)));

        var aliveUnitsActions = myTeam.units()
                                      .stream()
                                      .filter(unit -> unit.hasSpawned())
                                      .map(unit -> new UnitAction(UnitActionType.MOVE,
                                                                  unit.id(),
                                                                  getRandomPosition(map)));

        return Stream.concat(deadUnitsActions, aliveUnitsActions).toList();
    }

    private Position findRandomSpawn(GameMap map)
    {
        List<Position> spawns = new ArrayList<>();
        int x = 0;
        for (TileType[] tileX : map.tiles()) {
            int y = 0;
            for (TileType tileY : tileX) {
                if (tileY == TileType.SPAWN) {
                    spawns.add(new Position(x, y));
                }
                y++;
            }
            x++;
        }
        return spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()));
    }

    public Position getRandomPosition(GameMap map)
    {
        Random rand = ThreadLocalRandom.current();
        return new Position(rand.nextInt(map.horizontalSize()), rand.nextInt(map.verticalSize()));
    }
}