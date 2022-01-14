package codes.blitz.game.message.exception;

import codes.blitz.game.message.game.Position;

public class PositionOutOfMapException extends Exception
{
    private static final long serialVersionUID = 1L;

    public PositionOutOfMapException(String message)
    {
        super(message);
    }

    public PositionOutOfMapException(Position position)
    {
        super(String.format("Point %s is out of map!", position));
    }
}
