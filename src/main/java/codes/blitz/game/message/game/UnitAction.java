package codes.blitz.game.message.game;

public record UnitAction(ActionType type, UnitActionType action, String unitId, Position target) {
    public UnitAction(UnitActionType action, String unitId, Position target)
    {
        this(ActionType.UNIT, action, unitId, target);
    }
}
