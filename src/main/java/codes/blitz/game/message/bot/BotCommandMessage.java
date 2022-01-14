package codes.blitz.game.message.bot;

import java.util.List;

import codes.blitz.game.message.MessageType;
import codes.blitz.game.message.game.UnitAction;

public record BotCommandMessage(MessageType type, List<UnitAction> actions, Integer tick) {
    public BotCommandMessage(List<UnitAction> actions, Integer tick)
    {
        this(MessageType.COMMAND, actions, tick);
    }
}
