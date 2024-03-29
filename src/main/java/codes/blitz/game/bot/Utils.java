package codes.blitz.game.bot;

import java.util.*;

import codes.blitz.game.message.exception.PositionOutOfMapException;
import codes.blitz.game.message.game.*;
import org.glassfish.grizzly.utils.Pair;

public class Utils {



    public static void createUnitMap() {
        Bot.EnemyMap = new HashMap<>();

        Map<String, Team> teamsMapID = new HashMap<>(Map.copyOf(Bot.m_message.teamsMapById()));
        // retrait de notre équipe
        teamsMapID.remove(Bot.m_message.teamId());


        teamsMapID.forEach((s, team) -> {
            team.units().forEach((unit) -> Bot.EnemyMap.put(unit.position(), unit.id()));
        });
    }

    public static  int getSummonLevel(String id) {
        Position pos = Bot.PlayerMap.get(id);

        if (!Bot.DiamondMap.containsKey(pos)) {
            return -1;
        }

        return Bot.DiamondMap.get(pos).summonLevel();
    }

    public static void createPlayerMap() {
        Bot.PlayerMap = new HashMap<>();
        Bot.m_message.teamsMapById().get(Bot.m_message.teamId()).units().forEach((unit) -> {
            Bot.PlayerMap.put(unit.id(), unit.position());
        });
    }

    public static void createDiamondMap() {
        Bot.DiamondMap = new HashMap<>();

        Bot.m_message.map().diamonds().forEach((diamond) -> {
            Bot.DiamondMap.put(diamond.position(), diamond);
        });
    }

    public static void SetMap(GameMessage message) {
        Bot.m_message = message;
    }

    public static ArrayList<Position> findHeldDiamond() {

        ArrayList<Position> hDiamonds = new ArrayList<>();

        Bot.DiamondMap.forEach((pos, diamond) -> {
            if (diamond.points() > 0) {
                if (Bot.EnemyMap.containsKey(pos)) {
                    hDiamonds.add(pos);
                }
            }
        });

        return hDiamonds;
    }

    /**
     * @deprecated
     */
    public List<Position> findUnitsPosition() {
        List<Position> positions = new ArrayList<>();

        Bot.m_message.teamsMapById().remove(Bot.m_message.teamId());
        Bot.m_message.teams().forEach((team) -> {
            team.units().forEach((unit) -> {
                positions.add(unit.position());
            });
        });

        return positions;
    }
    public static void findPlayers()
    {
        Bot.m_message.teamsMapById().get(Bot.m_message.teamId()).units().forEach((unit) -> {
            Bot.PlayerMap.put(unit.id(), unit.position());
        });

    }

    public static List<Position> findPlayersPosition()
    {
        List<Position> positions = new ArrayList<>();

        Bot.m_message.teams().forEach((team) -> {
            team.units().forEach((unit) -> {
                if(unit.position() != null) {
                    positions.add(unit.position());
                }
            });
        });

        return positions;
    }

    public static List<Position> findAllyPosition() {
        List<Position> positions = new ArrayList<>();

        Bot.m_message.teamsMapById().get(Bot.m_message.teamId()).units().forEach((unit) -> {
            positions.add(unit.position());
        });
        return positions;
    }

    public static int getDistance(Position a, Position b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    public static void SortTile() throws PositionOutOfMapException {
        for (int i = 0; i < Bot.m_message.map().horizontalSize(); i++) {
            for (int j = 0; j < Bot.m_message.map().verticalSize(); j++) {
                Position pos = new Position(i, j);
                TileType type = Bot.m_message.map().tileTypeAt(pos);
                switch (type) {
                    case SPAWN:
                        Bot.spawnTiles.add(pos);
                        break;
                    case WALL:
                        Bot.wallTiles.add(pos);
                        break;
                    case EMPTY:
                        Bot.blankTile.add(pos);
                        break;
                }
            }
        }
    }

    /*
     * retourne la position de l'ennemi le plus proche ayant un diamant. S'il n'y en a pas, retourne null.
     */
    public static Position findEnemyPlayerWithDiamond(Position x) {
        Position enemyPos = null;
        int distance = 9999999;

        for (Map.Entry<Position, String> enemy : Bot.EnemyMap.entrySet()) {
            if (Bot.DiamondMap.containsKey(enemy.getKey())) {
                int cost = getDistance(x, enemy.getKey());
                if (
                        cost < distance
                                || (cost == distance && Bot.DiamondMap.get(enemy.getKey()).points() > Bot.DiamondMap.get(enemyPos).points())
                ) {
                    enemyPos = enemy.getKey();
                    distance = cost;
                }
            }
        }

        return enemyPos;
    }

    public static Position findNearestPlayer(Position x) {
        List<Position> players = findPlayersPosition();
        Position best = players.get(0);
        int cost = getDistance(x, best);

        for (int i = 1; i < players.size(); i++) {
            int intermediate_cost = getDistance(x, players.get(i));
            if (intermediate_cost < cost) {
                cost = intermediate_cost;
                best = players.get(i);
            }
        }

        return best;
    }

    public static Position isMenacer(Position x, int nb_tour) {
        if(nb_tour == 5)
        {
            nb_tour = 1;
        }

        for (Position enemyPos : Bot.EnemyMap.keySet())
        {
            if(enemyPos == null) continue;
            if (getDistance(enemyPos, x) <= nb_tour+1)
            {
                return enemyPos;
            }
        }
        return null;
    }

    /**
     * X is 1/-1
     * Y is 1/-1
     * Depends on direction of target depending on source
     * @param source
     * @param target
     * @return
     */
    public static Position directionOfTarget(Position source, Position target)
    {
        int coorX = 0, coorY = 0;
        if (source.x() - target.x() > 0)
        {
            coorX--;
        }
        else if (source.x() - target.x() < 0)
        {
            coorX++;
        }

        if (source.y() - target.y() > 0)
        {
            coorY--;
        }
        else if (source.y() - target.y() < 0)
        {
            coorY++;
        }
        return new Position(coorX, coorY);
    }

    public static boolean positionInList(Position position, List<Position> positions) {
        for (Position pos : positions) {
            if (position.x() == pos.x() && position.y() == pos.y()) {
                return true;
            }
        }
        return false;
    }

    public Position canVine(Unit playerUnit) throws PositionOutOfMapException {
        Position playerPosition = playerUnit.position();
        // Check if not in a spawn tile
        if (!positionInList(playerPosition, Bot.spawnTiles)) {
            SortTile();
            List<Position> positions = findPlayersToVine(playerPosition);
            if(positions.isEmpty() || playerUnit.hasDiamond()) return null;
            return positions.get(0); // À CHANGER SELON NOTRE STRATÉGIE
        }

        return null;
    }

    public List<Position> findPlayersToVine(Position playerPosition) {
        List<Position> playersPosition = findPlayersPosition();
        List<Position> positions = new ArrayList<>();

        playersPosition.forEach((position -> {
            if (!(position.x() == playerPosition.x() && position.y() == playerPosition.y())) { // not my position
                if (playerPosition.x() == position.x()) {
                    int pos_min = (Math.min(playerPosition.y(), position.y()));
                    int pos_max = (Math.max(playerPosition.y(), position.y()));

                    for (int i = pos_min; i <= pos_max; i++) {
                        if (!positionInList(new Position(position.x(), i), Bot.blankTile)) {
                            break;
                        }
                    }
                    positions.add(position);
                } else if (playerPosition.y() == position.y()) {
                    int pos_min = (Math.min(playerPosition.x(), position.x()));
                    int pos_max = (Math.max(playerPosition.x(), position.x()));

                    for (int i = pos_min; i <= pos_max; i++) {
                        if (!positionInList(new Position(i, position.x()), Bot.blankTile)) {
                            break;
                        }
                    }
                    positions.add(position);
                }
            }
        }));

        return positions;
    }

    public static Position findIfEnemyAdjacent(String id)
    {
        Position pos = Bot.PlayerMap.get(id);
        for (Position enemyPos: Bot.EnemyMap.keySet()) {
            if (enemyPos == null) continue;
            if (getDistance(enemyPos, pos) == 1)
            {
                return enemyPos;
            }
        }
        return null;
    }

    public static ArrayList<Position> findNearestDiamonds() {
        ArrayList<Position> nearDiamondPos = new ArrayList<Position>();
        for (Position p : Bot.PlayerMap.values()) {
            if(p == null) continue;
            int bestCost = Integer.MAX_VALUE;
            Position bestPos = new Position(0, 0);
            for (Position dpos : Bot.DiamondMap.keySet()) {
                int cost = getDistance(p, dpos);
                if (cost < bestCost && !nearDiamondPos.contains(dpos) && !findHeldDiamond().contains(dpos)) {
                    bestCost = cost;
                    bestPos = dpos;
                }
            }
            if(!(bestPos.x() == 0 && bestPos.y() == 0))
            {
                nearDiamondPos.add(bestPos);
            }
        }

        return nearDiamondPos;
    }

    public static ArrayList<Position> findNearestSpawn() {
        Map<Position, Boolean> removedDiamonds = new HashMap<>();
        ArrayList<Position> usedSpawn = new ArrayList<>(Bot.EnemyMap.keySet());
        Bot.PlayerMap.values().forEach((value)->{
            if(value != null)
            {
                usedSpawn.add(value);
            }
        });

        ArrayList<Pair<Position, Integer>> spawnPos = new ArrayList<>();
      //  ArrayList<Position> spawnPos = new ArrayList<>();

        int bestCost = 100000;
        Position bestPos = new Position(0, 0);

        Position diamondtoRemovePos = null;
        Position tileToRemovePos = null;
        for (Position dPos : Bot.DiamondMap.keySet()) {
            for (Position pos : Bot.spawnTiles) {
                int cost = getDistance(dPos, pos);
                if (cost < bestCost && !removedDiamonds.containsKey(dPos) && !usedSpawn.contains(pos)) {
                    bestCost = cost;
                    bestPos = pos;
                    tileToRemovePos = pos;
                    diamondtoRemovePos = dPos;
                }
            }
            if(Bot.spawnTiles.contains(bestPos) && checkBlockingSpawnPosition(bestPos))
            {
                spawnPos.add(new Pair<Position, Integer>(bestPos, bestCost));
                usedSpawn.add(tileToRemovePos);
                removedDiamonds.put(diamondtoRemovePos, true);
                bestCost = 100000;
                bestPos = new Position(0, 0);
            }
        }
        spawnPos.sort((o1, o2) -> {
            if(o1.getSecond() < o2.getSecond())
                return -1;
            else if(Objects.equals(o1.getSecond(), o2.getSecond()))
                return 0;
                else
                    return 1;
        });
        ArrayList<Position> pos = new ArrayList<>();
        spawnPos.forEach(positions->{
            pos.add(positions.getFirst());
        });
        return pos;
    }

    public static Position chaseEnemy(String id) {
        Position playerPosition = Bot.PlayerMap.get(id);
        Position minEnemyPos = null;
        int minDistance = 0;
        int currentDistance;

        for(Position enemyPos : Bot.EnemyMap.keySet()) {
            currentDistance = getDistance(enemyPos, playerPosition);
            if(minDistance > currentDistance) {
                minDistance = currentDistance;
                minEnemyPos = enemyPos;
            }
        }

        return minEnemyPos;
    }


    public static Position whereToDrop(Position playerPosition)
    {
        Position north = new Position(playerPosition.x(), playerPosition.y()-1);
        Position south = new Position(playerPosition.x(), playerPosition.y()+1);
        Position east = new Position(playerPosition.x()+1, playerPosition.y());
        Position west = new Position(playerPosition.x()-1, playerPosition.y());
        if (Bot.blankTile.contains(north) && !Bot.EnemyMap.keySet().contains(north) && !Bot.PlayerMap.values().contains(north)
                && north.x() >= 0 && north.x() < Bot.m_message.map().horizontalSize() &&
                north.y() >= 0 && north.y() < Bot.m_message.map().verticalSize())
        {
            return north;
        } else if (Bot.blankTile.contains(south) && !Bot.EnemyMap.keySet().contains(south) && !Bot.PlayerMap.values().contains(south)
                && south.x() >= 0 && south.x() < Bot.m_message.map().horizontalSize() &&
                south.y() >= 0 && south.y() < Bot.m_message.map().verticalSize())
        {
            return south;
        } else if (Bot.blankTile.contains(east) && !Bot.EnemyMap.keySet().contains(east) && !Bot.PlayerMap.values().contains(east)
                && east.x() >= 0 && east.x() < Bot.m_message.map().horizontalSize() &&
                east.y() >= 0 && east.y() < Bot.m_message.map().verticalSize())
        {
            return east;
        } else if (Bot.blankTile.contains(west) && !Bot.EnemyMap.keySet().contains(west) && !Bot.PlayerMap.values().contains(west)
                && west.x() >= 0 && west.x() < Bot.m_message.map().horizontalSize() &&
                west.y() >= 0 && west.y() < Bot.m_message.map().verticalSize())
        {
            return west;
        }
        else
        {
            return null;
        }
    }

    /**
     *
     * @param target
     * @param currPosition
     * @return
     */
    public static boolean isMovable(Position target, Position currPosition)
    {
        if(Bot.EnemyMap.containsKey(target) || Bot.wallTiles.contains(target) ||
                Bot.PlayerMap.containsValue(target) ||
                (Bot.spawnTiles.contains(target) && !Bot.spawnTiles.contains(currPosition)) ||
                target.x() < 0 || target.y() < 0 || target.x() >= Bot.m_message.map().horizontalSize()
                || target.y() >= Bot.m_message.map().verticalSize())
        {
            return false;
        }
        return true;
    }

//    public static int leftPosition(int pos, boolean isY) {
//        boolean isNotWall = true;
//        Position nextPosition;
//        int leftPos = pos;
//        if(pos > 0) {
//            for(int i = pos; isNotWall; i--) {
//                nextPosition = (isY ? new Position(pos, i): new Position(i, pos));
//                if (Bot.wallTiles.contains(nextPosition) || i < 0) {
//                    leftPos = i;
//                    isNotWall = false;
//                }
//            }
//        }
//        return leftPos;
//    }

    // old_position default value null
    public static boolean checkBlockingSpawnPosition(Position p) {
        return checkBlockingSpawnPosition(p, null);
    }

    public static boolean checkBlockingSpawnPosition(Position p, Position old_position) {
        ArrayList<Position> positions = new ArrayList<>(4);
        if (p.x() != Bot.m_message.map().horizontalSize()-1) {
            positions.add(new Position(p.x()+1, p.y()));
        }

        if (p.x() != 0) {
            positions.add(new Position(p.x()-1, p.y()));
        }

        if (p.y() != Bot.m_message.map().verticalSize()-1) {
            positions.add(new Position(p.x(), p.y()+1));
        }

        if (p.y() != 0) {
            positions.add(new Position(p.x(), p.y()-1));
        }

        if (old_position != null) {
            positions.removeIf(position -> position.x() == old_position.x() && position.y() == old_position.y());
        }

        for (Position position : positions) {
            if (Bot.blankTile.contains(position)) {
                     return true;
            }
        }

        for (Position position : positions) {
            if (Bot.spawnTiles.contains(position)) {
                System.out.println(position);
                return checkBlockingSpawnPosition(position, p);
            }
        }

        return false;
    }

}