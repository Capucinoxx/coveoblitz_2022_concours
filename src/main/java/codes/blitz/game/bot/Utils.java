package codes.blitz.game.bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codes.blitz.game.message.exception.PositionOutOfMapException;
import codes.blitz.game.message.game.*;

public class Utils {

    public static GameMessage m_message;
    public static Map<Position, Integer> DiamondMap;
    public static Map<Position, String> EnemyMap;
    public static Map<String, Position> PlayerMap;
    public static ArrayList<Position> spawnTiles = new ArrayList<Position>();
    public static ArrayList<Position> wallTiles = new ArrayList<Position>();
    public static ArrayList<Position> blankTile = new ArrayList<Position>();


    public static void createUnitMap() {
        EnemyMap = new HashMap<>();

        Map<String, Team> teamsMapID = m_message.teamsMapById();
        // retrait de notre équipe
        teamsMapID.remove(m_message.teamId());


        teamsMapID.forEach((s, team) -> {
            team.units().forEach((unit) -> EnemyMap.put(unit.position(), unit.id()));
        });
    }

    public static void createPlayerMap() {
        PlayerMap = new HashMap<>();

        m_message.teamsMapById().get(m_message.teamId()).units().forEach((unit) -> {
            PlayerMap.put(unit.id(), unit.position());
        });
    }

    public static void createDiamondMap() {
        DiamondMap = new HashMap<>();

        m_message.map().diamonds().forEach((diamond) -> {
            DiamondMap.put(diamond.position(), diamond.points());
        });
    }

    public void SetMap(GameMessage message)
    {
        m_message = message;
    }

    public ArrayList<Position> findHeldDiamond() {
        ArrayList<Position> hDiamonds = new ArrayList<>();

        DiamondMap.forEach((pos, val) -> {
            if (val > 0) {
                if (EnemyMap.containsKey(pos)) {
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

        m_message.teamsMapById().remove(m_message.teamId());
        m_message.teams().forEach((team) -> {
            team.units().forEach((unit) -> {
                positions.add(unit.position());
            });
        });

        return positions;
    }
    public static void findPlayers()
    {
        m_message.teamsMapById().get(m_message.teamId()).units().forEach((unit) -> {
            PlayerMap.put(unit.id(), unit.position());
        });

    }

    public List<Position> findPlayersPosition()
    {
        List<Position> positions = new ArrayList<>();

        m_message.teams().forEach((team) -> {
            team.units().forEach((unit) -> {
                positions.add(unit.position());
            });
        });

        return positions;
    }

    public List<Position> findAllyPosition() {
        List<Position> positions = new ArrayList<>();

        m_message.teamsMapById().get(m_message.teamId()).units().forEach((unit) -> {
            positions.add(unit.position());
        });
        return positions;
    }

    public int getDistance(Position a, Position b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    public static void SortTile() throws PositionOutOfMapException {
        for(int i = 0; i < m_message.map().horizontalSize(); i++)
        {
            for(int j = 0; j < m_message.map().verticalSize(); j++)
            {
                Position pos = new Position(i, j);
                TileType type = m_message.map().tileTypeAt(pos);
                switch (type) {
                    case SPAWN:
                        spawnTiles.add(pos);
                    case WALL:
                        wallTiles.add(pos);
                    case EMPTY:
                        blankTile.add(pos);
                }
            }
        }
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

    public boolean isMenacer(Position x) {
        for (int i = 0; i < Bot.taha_fait_vraiment_chier; i++) {
            for (int j = 0; j < Bot.taha_fait_vraiment_chier; j++) {
                if (EnemyMap.containsKey(new Position(x.x() + i, x.y() + j))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean positionInList(Position position, List<Position> positions) {
        for(Position pos : positions) {
            if (position.x() == pos.x() && position.y() == pos.y()) {
                return true;
            }
        }
        return false;
    }

    public void canVine(Position playerPosition) {
        // Check if not in a spawn tile
        if(!positionInList(playerPosition, spawnTiles)) {

        }
    }

    public List<Position> checkSameLine(Position playerPosition) {
        List<Position> playersPosition = findPlayersPosition();
        List<Position> positions = new ArrayList<>();

        playersPosition.forEach((position -> {
            if(!(position.x() == playerPosition.x() && position.y() == playerPosition.y())) { // not my position
                if(playerPosition.x() == position.x()) {
                    int pos_min = (Math.min(playerPosition.y(), position.y()));
                    int pos_max = (Math.max(playerPosition.y(), position.y()));

                    for(int i = pos_min; i <= pos_max; i++) {
                        if(!positionInList(new Position(position.x(), i), blankTile)) {
                            break;
                        }
                    }
                    positions.add(position);
                } else if (playerPosition.y() == position.y()){
                    int pos_min = (Math.min(playerPosition.x(), position.x()));
                    int pos_max = (Math.max(playerPosition.x(), position.x()));

                    for(int i = pos_min; i <= pos_max; i++) {
                        if(!positionInList(new Position(i, position.x()), blankTile)) {
                            break;
                        }
                    }
                    positions.add(position);
                }
            }
        }));

        return positions;
    }

    public static Boolean findIfEnemyAdjacent(String id)
    {
        Boolean canAttack = false;
        Position pos = PlayerMap.get(id);
        for (Position enemyPos: EnemyMap.keySet()) {
            if((enemyPos.x() == pos.x() + 1 || enemyPos.x() == pos.x() - 1)
            && (enemyPos.y() != pos.y() + 1 || enemyPos.y() != pos.y() - 1))
            {
               canAttack = true;
            }
        }
        return canAttack;
    }



}