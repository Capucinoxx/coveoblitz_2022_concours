package codes.blitz.game.message.game;

import java.util.List;

import codes.blitz.game.message.exception.PositionOutOfMapException;

public record GameMap(TileType[][] tiles, List<Diamond> diamonds) {

    public int horizontalSize()
    {
        return this.tiles.length;
    }

    public int verticalSize()
    {
        return this.tiles[0].length;
    }

    public TileType tileTypeAt(Position position) throws PositionOutOfMapException
    {
        return this.rawTileValueAt(position);
    }

    public TileType rawTileValueAt(Position position) throws PositionOutOfMapException
    {
        this.validateTileExists(position);
        return this.tiles[position.x()][position.y()];
    }

    public void validateTileExists(Position position) throws PositionOutOfMapException
    {
        if (position.x() < 0 || position.y() < 0 || position.x() >= this.horizontalSize()
                || position.y() >= this.verticalSize()) {
            throw new PositionOutOfMapException(position);
        }
    }
}