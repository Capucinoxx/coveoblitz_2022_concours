/*
 * Copyright (c) Coveo Solutions Inc.
 */
package codes.blitz.game.message.game;

public record UnitState(Position positionBefore, String wasVinedBy, String wasAttackedBy) {
}
