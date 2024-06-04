import java.util.ArrayList;
import java.awt.Color;

/**
 * Represents a snake in the game.
 * The snake has a head position, a body composed of tiles, a velocity,
 * a color, a name, and a status indicating if it is dead.
 */
public class Snake {
    /** The name of the snake. */
    String name;
    
    /** The position of the snake's head. */
    Tile head;
    
    /** The body of the snake, represented as a list of tiles. */
    ArrayList<Tile> body;

    /** The velocity of the snake in the X direction. */
    int velocityX;
    
    /** The velocity of the snake in the Y direction. */
    int velocityY;

    /** The status indicating if the snake is dead. */
    boolean isDead;

    /** The color of the snake. */
    Color color;

    /**
     * Constructs a new Snake object.
     *
     * @param position the initial position of the snake's head
     * @param velocityX the initial velocity of the snake in the X direction
     * @param velocityY the initial velocity of the snake in the Y direction
     * @param color the color of the snake
     * @param name the name of the snake
     */
    Snake(Tile position, int velocityX, int velocityY, Color color, String name) {
        head = position;
        body = new ArrayList<Tile>();

        this.velocityX = velocityX;
        this.velocityY = velocityY;

        this.color = color;
        this.name = name;

        isDead = false;
    }
}

