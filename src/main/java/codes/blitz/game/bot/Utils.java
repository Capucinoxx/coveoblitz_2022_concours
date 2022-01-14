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

    public List findPlayerPosition()
    {
        for (m_message.teams() : )
    }

    public List findAllyPosition()
    {

    }
}