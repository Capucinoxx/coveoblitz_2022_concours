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
    public static Map<Position, Boolean> UnitMap;
    public static ArrayList<Position> spawnTiles = new ArrayList<Position>();
    public static ArrayList<Position> wallTiles = new ArrayList<Position>();
    public static ArrayList<Position> blankTile = new ArrayList<Position>();

    public static void createUnitMap() {
        UnitMap = new HashMap<>();

        Map<String, Team> teamsMapID = m_message.teamsMapById();
        // retrait de notre équipe
        teamsMapID.remove(m_message.teamId());


        teamsMapID.forEach((s, team) -> { team.units().forEach((unit) -> UnitMap.put(unit.position(), true)); });

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
                if (UnitMap.containsKey(pos)) {
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
                if (UnitMap.containsKey(new Position(x.x() + i, x.y() + j))) {
                    return true;
                }
            }
        }
        return false;
    }
}