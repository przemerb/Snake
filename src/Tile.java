/**
 * Represents a tile in the game grid.
 * A tile has an x and y coordinate.
 */
public class Tile {
    /** The x-coordinate of the tile. */
    int x;
    
    /** The y-coordinate of the tile. */
    int y;

    /**
     * Constructs a new Tile object with the specified coordinates.
     *
     * @param x the x-coordinate of the tile
     * @param y the y-coordinate of the tile
     */
    Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
