import java.util.ArrayList;
import java.awt.Color;

public class Snake {
    String name;
    Tile head;
    ArrayList<Tile> body;

    int velocityX;
    int velocityY;

    boolean isDead;

    Color color;

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
