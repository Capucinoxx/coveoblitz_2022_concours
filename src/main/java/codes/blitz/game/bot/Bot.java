package codes.blitz.game.bot;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import codes.blitz.game.message.exception.PositionOutOfMapException;
import codes.blitz.game.message.game.*;
import jdk.jshell.execution.Util;

public class Bot
{
    public static final Integer taha_fait_vraiment_chier = 5;

    public static GameMessage m_message;

    public static Map<Position, String> EnemyMap;
    public static Map<String, Position> PlayerMap;
    public static Map<Position, Diamond> DiamondMap = new HashMap<>();
    public static ArrayList<Position> spawnTiles = new ArrayList<Position>();
    public static ArrayList<Position> wallTiles = new ArrayList<Position>();
    public static ArrayList<Position> blankTile = new ArrayList<Position>();

    public Bot()
    {
        System.out.println("Initializing your super duper mega bot.");
        // initialize some variables you will need throughout the game here
    }

//     * Here is where the magic happens, for now the moves are random. I bet you can do better ;)
//     *
//     * No path finding is required, you can simply send a destination per unit and the game will move your unit towards
//     * it in the next turns.


    public List<UnitAction> getNextActions(GameMessage gameMessage)
    {
        m_message = gameMessage;
        Utils.createDiamondMap();
        Utils.createUnitMap();
        Utils.createPlayerMap();
        Utils.findPlayers();
        if (gameMessage.tick() == 1)
        {
            try {
                Utils.SortTile();
            } catch (PositionOutOfMapException e) {

            }
        }


        Team myTeam = gameMessage.teamsMapById().get(gameMessage.teamId());
        GameMap map = gameMessage.map();

        var remainingUnits = myTeam.units();
        List<UnitAction> allActions = new ArrayList<>();

        for (Unit currUnit : remainingUnits)
        {

            UnitAction tempStream = new UnitAction(UnitActionType.MOVE,
                    currUnit.id(),
                    getRandomPosition(map));

            if(!currUnit.hasSpawned()){
                tempStream = new UnitAction(UnitActionType.SPAWN,
                        currUnit.id(),
                        Utils.findNearestSpawn().get(Integer.parseInt(currUnit.id()) - 1));
                allActions.add(tempStream);
                continue;
            }

            List<Position> nearestDiamonds = Utils.findNearestDiamonds();
            Position enemyPos = Utils.findIfEnemyAdjacent(currUnit.id());
            if (!currUnit.hasDiamond() && enemyPos != null && !spawnTiles.contains(enemyPos) && !spawnTiles.contains(currUnit))
            {
                tempStream = new UnitAction(UnitActionType.ATTACK,
                        currUnit.id(),
                        enemyPos);
            }
            else if (!currUnit.hasDiamond() && !(nearestDiamonds.size() <= Integer.parseInt(currUnit.id())-1)) {
            tempStream = new UnitAction(UnitActionType.MOVE,
                    currUnit.id(),
                    nearestDiamonds.get(Integer.parseInt(currUnit.id()) - 1));
            }
            else if (currUnit.hasDiamond() && gameMessage.tick() == gameMessage.totalTick()-1)
            {
                tempStream = new UnitAction(UnitActionType.DROP,
                        currUnit.id(),
                        new Position(currUnit.position().x()+1, currUnit.position().y()));
            }
            else if (currUnit.hasDiamond() && Utils.isMenacer(currUnit.position(), Utils.getSummonLevel(currUnit.id())) != null)
            {
                tempStream = new UnitAction(UnitActionType.DROP,
                                currUnit.id(),
                            new Position(currUnit.position().x()+1, currUnit.position().y()));
            }
            else if (currUnit.hasDiamond() && Utils.getSummonLevel(currUnit.id()) != 5)
            {
                tempStream = new UnitAction(UnitActionType.SUMMON,
                        currUnit.id(),
                        currUnit.position());
            }

            allActions.add(tempStream);
        }
        return allActions;
//        var deadUnitsActions = myTeam.units()
//                .stream()
//                .filter(unit -> !unit.hasSpawned())
//                .map(unit -> new UnitAction(UnitActionType.SPAWN,
//                        unit.id(),
//                        findRandomSpawn(map)));
//
//        var aliveUnitsActions = myTeam.units()
//                .stream()
//                .filter(unit -> unit.hasSpawned() && !unit.hasDiamond())
//                .map(unit -> new UnitAction(UnitActionType.MOVE,
//                        unit.id(),
//                        Utils.findNearestDiamonds().get(Integer.parseInt(unit.id())-1)
//                ));
//
//        var hasDiamondSafeActions = myTeam.units().stream().filter(unit -> unit.hasDiamond() &&
//                        gameMessage.tick() != gameMessage.totalTick()-1)
//                .map(unit -> new UnitAction(UnitActionType.SUMMON,
//                        unit.id(),
//                        unit.position()));
//
//        var hasDiamondActions = myTeam.units().stream().filter(unit -> unit.hasDiamond() &&
//                        Utils.isMenacer(unit.position()))
//                .map(unit -> new UnitAction(UnitActionType.DROP,
//                        unit.id(),
//                        getRandomPosition(map)));
//
//        var lastTurnActions = myTeam.units().stream().filter(unit -> unit.hasDiamond() &&
//                        gameMessage.tick() == gameMessage.totalTick()-1)
//                .map(unit -> new UnitAction(UnitActionType.DROP,
//                        unit.id(),
//                        getRandomPosition(map)));
//
//        var actions = Stream.concat(deadUnitsActions, hasDiamondActions);
//        actions = Stream.concat(actions, lastTurnActions);
//        actions = Stream.concat(actions, hasDiamondSafeActions);
//        return Stream.concat(actions, aliveUnitsActions).toList();
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
