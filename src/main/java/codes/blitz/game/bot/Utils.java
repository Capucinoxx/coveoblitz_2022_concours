package codes.blitz.game.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import codes.blitz.game.message.game.*;

public class Utils {

    public GameMessage m_message;
    public Map<Position, Integer> DiamondMap;

    public void CreateDiamondMap()
    {
        m_message.map().diamonds().forEach((diamond) -> {
            DiamondMap.put(diamond.position(), diamond.points());
        });
    }

    public void SetMap(GameMessage message)
    {
        m_message = message;
    }

    public void findHeldDiamond() {
        Map.Entry<Position, Integer> entry;
        ArrayList<Position> unitsPosition = findUnitsPosition();
        ArrayList<Position> hDiamonds = new ArrayList<>();
        DiamondMap.forEach((pos, val) -> {
            if (val > 0) {
                for (Position p : unitsPosition) {
                    if (p == pos) {
                        hDiamonds.add(pos);
                    }
                }
            }
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

}