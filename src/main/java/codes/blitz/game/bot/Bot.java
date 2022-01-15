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

        ArrayList<Position> spawnPos = new ArrayList<>();
        if (gameMessage.tick() == 0)
        {
            try {
                Utils.SortTile();
            } catch (PositionOutOfMapException e) {
                System.out.println("didnt sort");
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
                if(spawnPos.isEmpty())
                {
                    System.out.println(spawnPos);
                    spawnPos = Utils.findNearestSpawn();
                }
                System.out.println(spawnPos);
                try{
                    tempStream = new UnitAction(UnitActionType.SPAWN,
                            currUnit.id(),
                            spawnPos.get(Integer.parseInt(currUnit.id()) - 1));
                    allActions.add(tempStream);
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("RANDOM  SPAWN");
                    tempStream = new UnitAction(UnitActionType.SPAWN,
                            currUnit.id(),
                            findRandomSpawn(map));
                    allActions.add(tempStream);
                }
                continue;
            }
            Map<String, Position> mapNearestDiamond;
            ArrayList<Position> nearestDiamonds = Utils.findNearestDiamonds();
            Position enemyDiamondPos = Utils.findEnemyPlayerWithDiamond(currUnit.position());
            if ((!currUnit.hasDiamond() && nearestDiamonds.size() > Integer.parseInt(currUnit.id())-1) ||
                    (!currUnit.hasDiamond() && enemyDiamondPos != null && Utils.getDistance(currUnit.position(), enemyDiamondPos) > 1)) {

                if(enemyDiamondPos != null && (nearestDiamonds.size() <= Integer.parseInt(currUnit.id()) - 1 || Utils.getDistance(nearestDiamonds.get(Integer.parseInt(currUnit.id()) - 1), currUnit.position()) >
                        Utils.getDistance(Utils.whereToDrop(enemyDiamondPos), currUnit.position())))
                {
                    tempStream = new UnitAction(UnitActionType.MOVE,
                        currUnit.id(),
                        Utils.whereToDrop(enemyDiamondPos));
                }
                else
                {
                    tempStream = new UnitAction(UnitActionType.MOVE,
                            currUnit.id(),
                            nearestDiamonds.get(Integer.parseInt(currUnit.id()) - 1));
                }
                allActions.add(tempStream);
                continue;

            }


            if(!currUnit.hasDiamond() && enemyDiamondPos != null && Utils.getDistance(currUnit.position(), enemyDiamondPos) == 1 && !spawnTiles.contains(currUnit.position())) {
                tempStream = new UnitAction(UnitActionType.ATTACK, currUnit.id(), enemyDiamondPos);
            }

             /*Position enemyPos = Utils.findIfEnemyAdjacent(currUnit.id());
            if (!currUnit.hasDiamond() && enemyPos != null && !spawnTiles.contains(enemyPos) && !spawnTiles.contains(currUnit))
            {
                tempStream = new UnitAction(UnitActionType.ATTACK,
                        currUnit.id(),
                        enemyPos);
            }
            else*/
            else if (currUnit.hasDiamond() && gameMessage.tick() == gameMessage.totalTick()-1)
            {
                tempStream = new UnitAction(UnitActionType.DROP,
                        currUnit.id(),
                        Utils.whereToDrop(currUnit.position()));
            }
            else if (currUnit.hasDiamond() && Utils.isMenacer(currUnit.position(), Utils.getSummonLevel(currUnit.id())) != null)
            {
                Position enemyPos = Utils.isMenacer(currUnit.position(), Utils.getSummonLevel(currUnit.id()));
                if(Utils.getDistance(currUnit.position(), Objects.requireNonNull(enemyPos)) > 2 )
                {
                    Position whereGo = Utils.directionOfTarget(enemyPos, currUnit.position());
                    Position tryMove = new Position(currUnit.position().x()+ whereGo.x(), currUnit.position().y()+ whereGo.y());
                    if(Utils.isMovable(tryMove, currUnit.position()))
                    {
                        tempStream = new UnitAction(UnitActionType.MOVE,
                                currUnit.id(),
                                Utils.whereToDrop(currUnit.position()));
                    }
                    else
                    {
                        tempStream = new UnitAction(UnitActionType.DROP,
                                currUnit.id(),
                                Utils.whereToDrop(currUnit.position()));
                    }
                }
                else
                {
                    tempStream = new UnitAction(UnitActionType.DROP,
                            currUnit.id(),
                            Utils.whereToDrop(currUnit.position()));
                }
            }
            else if (currUnit.hasDiamond() && Utils.getSummonLevel(currUnit.id()) <= 5 &&
                    m_message.tick()-m_message.totalTick() >  Utils.getSummonLevel(currUnit.id())+1)
            {
                tempStream = new UnitAction(UnitActionType.SUMMON,
                        currUnit.id(),
                        currUnit.position());
            }

            allActions.add(tempStream);
        }
        return allActions;
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
