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

public class Utils {

    public GameMessage m_message;


    public void SetMap(GameMessage message)
    {
        m_message = message;
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