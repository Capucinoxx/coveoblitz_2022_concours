/*
 * Copyright (c) Coveo Solutions Inc.
 */
package codes.blitz.game.message.game;

public record Diamond(String id, Position position, int summonLevel, int points, String ownerId) {
}
