import javax.swing.*;
import java.awt.*;

/**
 * The main class that sets up and starts the Snake game.
 */
public class App {
    /**
     * The main method that initializes the game window and starts the Snake game.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during execution
     */
    public static void main(String[] args) throws Exception {
        // Get the screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Calculate the board size to be 80% of the screen size
        int boardWidth = (int) (screenWidth * 0.8);
        int boardHeight = (int) (screenHeight * 0.8);

        // Adjust the board size to be a multiple of 20
        boardWidth = (boardWidth / 20) * 20;
        boardHeight = (boardHeight / 20) * 20;

        // Create and set up the game window
        JFrame frame = new JFrame("Snake");
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and add the SnakeGame component
        SnakeGame snakeGame = new SnakeGame(boardWidth, boardHeight);
        frame.add(snakeGame);
        frame.pack();
        snakeGame.requestFocus();
    }
}

