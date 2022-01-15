package codes.blitz.game.bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codes.blitz.game.message.exception.PositionOutOfMapException;
import codes.blitz.game.message.game.*;

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

    public List<Position> findPlayersPosition()
    {
        List<Position> positions = new ArrayList<>();

        Bot.m_message.teams().forEach((team) -> {
            team.units().forEach((unit) -> {
                positions.add(unit.position());
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
                    case WALL:
                        Bot.wallTiles.add(pos);
                    case EMPTY:
                        Bot.blankTile.add(pos);
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

    public Position findNearestPlayer(Position x) {
        List<Position> players = this.findPlayersPosition();
        Position best = players.get(0);
        int cost = this.getDistance(x, best);

        for (int i = 1; i < players.size(); i++) {
            int intermediate_cost = this.getDistance(x, players.get(i));
            if (intermediate_cost < cost) {
                cost = intermediate_cost;
                best = players.get(i);
            }
        }

        return best;
    }

    public static Position isMenacer(Position x, int nb_tour) {
        for (int i = 0; i < nb_tour; i++) {
            for (int j = 0; j < nb_tour; j++) {
                Position pos = new Position(x.x() + i, x.y() + j);
                if (Bot.EnemyMap.containsKey(pos)) {
                    return pos;
                }
            }
        }
        return null;
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
        Boolean canAttack = false;
        Position pos = Bot.PlayerMap.get(id);
        for (Position enemyPos: Bot.EnemyMap.keySet()) {
            if(enemyPos == null)
            {
                continue;
            }
            if((enemyPos.x() == pos.x() + 1 || enemyPos.x() == pos.x() - 1)
                && (enemyPos.y() != pos.y() + 1 || enemyPos.y() != pos.y() - 1))
            {
               return enemyPos;
            }
            if((enemyPos.y() == pos.y() + 1 || enemyPos.y() == pos.y() - 1)
                    && (enemyPos.x() != pos.x() + 1 || enemyPos.x() != pos.x() - 1))
            {
                return enemyPos;
            }
        }
        return null;
    }

    public static ArrayList<Position> findNearestDiamonds() {
        ArrayList<Position> nearDiamondPos = new ArrayList<Position>();
        List<Position> allyPos = findAllyPosition();
        for (Position p : allyPos) {
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
        ArrayList<Position> usedSpawn = new ArrayList<>();
        ArrayList<Position> spawnPos = new ArrayList<>();

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
            usedSpawn.add(tileToRemovePos);
            removedDiamonds.put(diamondtoRemovePos, true);
            spawnPos.add(bestPos);
        }
        return spawnPos;
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
}