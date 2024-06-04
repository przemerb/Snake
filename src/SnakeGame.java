import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.swing.*;

/**
 * The SnakeGame class is responsible for managing the game state and rendering
 * the game components.
 * It implements the ActionListener and KeyListener interfaces to handle game
 * logic and user input.
 * 
 */
public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    /**
 * The width of the game board.
 */
int boardWidth;

/**
 * The height of the game board.
 */
int boardHeight;

/**
 * The size of each tile on the game board.
 */
int tileSize = 20;

/**
 * A list of snakes in the game.
 */
ArrayList<Snake> snakes;

/**
 * A list of threads for each snake, handling their movement.
 */
ArrayList<Thread> snakeThreads;

/**
 * A list of obstacles on the game board, each represented by a list of tiles.
 */
ArrayList<ArrayList<Tile>> obstacles;

/**
 * The apple tile on the game board.
 */
Tile apple;

/**
 * A random number generator for placing the apple and other random events.
 */
Random random;

/**
 * The frog tile on the game board.
 */
Tile frog;

/**
 * The thread handling the frog's movement.
 */
Thread frogThread;

/**
 * A flag indicating whether the frog has been eaten.
 */
boolean eatenFrog = false;

/**
 * The timer handling the game loop.
 */
Timer gameLoop;

/**
 * The file where scores are saved.
 */
File scoreFile = new File("scores.txt");

    /**
     * Constructs a new SnakeGame instance with the specified board dimensions.
     *
     * @param boardWidth  the width of the game board
     * @param boardHeight the height of the game board
     */
    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);
        random = new Random();

        newGame();
    }

    /**
     * Creates and starts a new thread for the specified snake.
     *
     * @param snake the snake for which to create a new thread
     */
    private void SnakeThread(Snake snake) {
        Thread snakeThread = new Thread(new Runnable() {
            public void run() {
                while (!snake.isDead) {
                    if (snake.name != "player") {
                        MakeNextMove(snake);
                    }
                    move(snake);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    repaint();
                }
            }
        });
        snakeThreads.add(snakeThread);
        snakeThread.start();
    }

    /**
     * Paints the game components onto the game board.
     *
     * @param g the Graphics object used to paint the game components
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draws the game components including the grid, food, obstacles, and snakes.
     *
     * @param g the Graphics object used to draw the game components
     */
    public void draw(Graphics g) {
        // Grid
        for (int i = 0; i < boardWidth / tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, boardHeight);
            g.drawLine(0, i * tileSize, boardWidth, i * tileSize);
        }
        // Food
        g.setColor(Color.red);
        g.fill3DRect(apple.x * tileSize, apple.y * tileSize, tileSize, tileSize, true);

        // Obstacle
        g.setColor(Color.gray);
        for (ArrayList<Tile> obstacle : obstacles) {
            for (Tile tile : obstacle) {
                g.fill3DRect(tile.x * tileSize, tile.y * tileSize, tileSize, tileSize, true);
            }
        }

        // Frog
        g.setColor(Color.green);
        g.fill3DRect(frog.x * tileSize, frog.y * tileSize, tileSize, tileSize, true);

        for (Snake snake : snakes) {
            // Snake Head
            g.setColor(snake.color);
            g.fill3DRect(snake.head.x * tileSize, snake.head.y * tileSize, tileSize, tileSize, true);

            g.setColor(snake.color);
            // Snake body
            for (int i = 0; i < snake.body.size(); i++) {
                Tile snakePart = snake.body.get(i);
                g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
            }
        }

        // Score
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (snakes.get(0).isDead) {

        } else {
            int i = 1;
            for (Snake snake : snakes) {
                g.setColor(snake.color);
                g.drawString("Score: " + String.valueOf(snake.body.size()), tileSize - 16, tileSize * i);
                i++;
            }
        }
    }

    /**
     * Generates a new random position for the tile, ensuring it does not collide
     * with any obstacles.
     *
     * @return a new Tile object representing the new position
     */
    public Tile new_place() {
        Tile newPlace;
        boolean colides;
        do {
            int x = random.nextInt(boardWidth / tileSize);
            int y = random.nextInt(boardHeight / tileSize);
            newPlace = new Tile(x, y);
            colides = false;
            for (ArrayList<Tile> obstacle : obstacles) {
                for (Tile tile : obstacle) {
                    if (collision(newPlace, tile)) {
                        colides = true;
                        break;
                    }
                }
            }
        } while (colides);
        return newPlace;
    }

    /**
     * Places obstacles randomly on the game board.
     */
    public void placeObstacles() {
        for (int i = 0; i < 3; ++i) {
            ArrayList<Tile> row = new ArrayList<>();
            obstacles.add(row);
            int x = random.nextInt((boardWidth / tileSize) - 6);
            int y = random.nextInt((boardHeight / tileSize) - 6);
            int direction = random.nextInt(6);
            for (int j = 0; j < 3; ++j) {
                switch (direction) {
                    case 0:
                        row.add(new Tile(x + j, y));
                        break;
                    case 1:
                        row.add(new Tile(x - j, y));
                        break;
                    case 2:
                        row.add(new Tile(x, y + j));
                        break;
                    case 3:
                        row.add(new Tile(x, y - j));
                        break;
                    case 4:
                        row.add(new Tile(x + j, y + j));
                        break;
                    case 5:
                        row.add(new Tile(x - j, y - j));
                        break;
                }

            }
        }
    }

    /**
     * Checks if two tiles collide (i.e., occupy the same position).
     *
     * @param tile1 the first tile
     * @param tile2 the second tile
     * @return true if the tiles collide, false otherwise
     */
    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    /**
     * Moves the specified snake and handles interactions such as eating food or
     * colliding with obstacles.
     *
     * @param snake the snake to be moved
     */
    public void move(Snake snake) {
        // eat apple
        if (collision(snake.head, apple)) {
            snake.body.add(new Tile(apple.x, apple.y));
            apple = new_place();
        }

        // eat frog
        if (collision(snake.head, frog)) {
            for (int i = 0; i < 3; i++) {
                snake.body.add(new Tile(frog.x, frog.y));
            }
            frog = new_place();
            eatenFrog = true;
        }

        // Snake Body
        for (int i = snake.body.size() - 1; i >= 0; i--) {
            Tile snakePart = snake.body.get(i);
            if (i == 0) {
                snakePart.x = snake.head.x;
                snakePart.y = snake.head.y;
            } else {
                Tile prevSnakePart = snake.body.get(i - 1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }

        // Snake Head
        snake.head.x += snake.velocityX;
        snake.head.y += snake.velocityY;

        // game over conditions
        for (int i = 0; i < snake.body.size(); i++) {
            Tile snakePart = snake.body.get(i);
            // collide with the snake head
            if (collision(snake.head, snakePart)) {
                snake.isDead = true;
            }
        }

        // check collision with other snakes
        for (Snake otherSnake : snakes) {
            if (otherSnake != snake) {
                // Check collision with the head of other snakes
                if (collision(snake.head, otherSnake.head)) {
                    snake.isDead = true;
                    otherSnake.isDead = true;
                }
                // Check collision with the body of other snakes
                for (Tile otherSnakePart : otherSnake.body) {
                    if (collision(snake.head, otherSnakePart)) {
                        snake.isDead = true;
                    }
                }
            }
        }

        // check collision with the frame
        if (snake.head.x * tileSize < 0 || snake.head.x * tileSize > boardWidth ||
                snake.head.y * tileSize < 0 || snake.head.y * tileSize > boardHeight) {
            snake.isDead = true;
        }

        // check collision with obstacles
        for (ArrayList<Tile> obstacle : obstacles) {
            for (Tile tile : obstacle) {
                if (collision(snake.head, tile)) {
                    snake.isDead = true;
                }
            }
        }
    }

    /**
     * Displays a game over dialog with the final scores and an option to play again
     * or close the game.
     * This method creates a modal dialog window containing the following
     * components:
     * - A label showing the game over message and the player's score.
     * - Labels showing the scores of AI snakes, if any.
     * - Buttons to either play again or close the game.
     * - A text area displaying the top 10 high scores.
     * The dialog is packed and positioned relative to the parent component.
     */
    private void showGameOverDialog() {
        JDialog gameOverDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Game Over", true);
        gameOverDialog.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        GridBagConstraints d = new GridBagConstraints();
        d.insets = new Insets(10, 10, 10, 10);

        JLabel scoreLabel = new JLabel("Game Over! Your score: " + snakes.get(0).body.size());
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        gameOverDialog.add(scoreLabel, c);
        for (int i = 1; i < snakes.size(); ++i) {
            JLabel scoreLabelAI = new JLabel("AI1 score: " + snakes.get(i).body.size());
            d.gridx = 0;
            d.gridy = 30 * i;
            d.gridwidth = 2;
            gameOverDialog.add(scoreLabelAI, d);
        }

        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGame();
                gameOverDialog.dispose();
            }
        });
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        gameOverDialog.add(playAgainButton, c);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        c.gridx = 1;
        c.gridy = 1;
        gameOverDialog.add(closeButton, c);

        JTextArea highScoresText = new JTextArea(10, 20);
        highScoresText.setEditable(false);
        highScoresText.setText(getHighScores());
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        gameOverDialog.add(new JScrollPane(highScoresText), c);

        gameOverDialog.pack();
        gameOverDialog.setLocationRelativeTo(this);
        gameOverDialog.setVisible(true);
    }

    /**
     * Starts a new game by initializing game components and starting the game loop.
     */
    private void newGame() {
        obstacles = new ArrayList<ArrayList<Tile>>();
        placeObstacles();

        snakes = new ArrayList<Snake>();
        Snake player = new Snake(new_place(), 0, 1, Color.blue, "player");
        snakes.add(player);
        Snake bot = new Snake(new_place(), 1, 0, Color.orange, "bot");
        snakes.add(bot);
        Snake bot2 = new Snake(new_place(), 0, -1, Color.yellow, "bot2");
        snakes.add(bot2);

        apple = new_place();

        frog = new_place();
        FrogThread();

        snakeThreads = new ArrayList<Thread>();
        for (Snake snake : snakes) {
            SnakeThread(snake);
        }

        // Restart the game loop
        gameLoop = new Timer(200, this);
        gameLoop.start();

        // Refresh the game display
        repaint();
    }

    /**
     * Saves the player's score along with the current date and time to a file.
     * This method appends the score and timestamp to the specified file in the
     * format "score timestamp".
     * If an IOException occurs during file writing, the stack trace is printed.
     *
     * @param score The player's score to be saved.
     */
    private void saveScore(int score) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile, true))) {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println(score + " " + date);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the top 10 high scores from the score file.
     * This method reads scores from the score file, sorts them in descending order,
     * and returns the top 10 scores.
     * Scores are formatted as "score timestamp".
     * If an IOException occurs during file reading, the stack trace is printed.
     *
     * @return A string containing the top 10 high scores formatted as "score
     *         timestamp".
     */
    private String getHighScores() {
        List<String> scores = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(scoreFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                scores.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        scores.sort((a, b) -> {
            int scoreA = Integer.parseInt(a.split(" ")[0]);
            int scoreB = Integer.parseInt(b.split(" ")[0]);
            return Integer.compare(scoreB, scoreA); // Descending order
        });
        StringBuilder topScores = new StringBuilder("Top 10 High Scores:\n");
        for (int i = 0; i < Math.min(10, scores.size()); i++) {
            topScores.append(scores.get(i)).append("\n");
        }
        return topScores.toString();
    }

    /**
     * Creates and starts a thread responsible for moving the frog on the game
     * board.
     * The frog moves continuously while the player snake is alive and the frog
     * hasn't been eaten.
     * If the frog is eaten, the thread restarts to spawn a new frog.
     */
    private void FrogThread() {
        frogThread = new Thread(() -> {
            while (!snakes.get(0).isDead && !eatenFrog) {
                moveFrog();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                repaint();
            }
            if (eatenFrog) {
                eatenFrog = false;
                FrogThread();
            }
        });
        frogThread.start();
    }

    /**
     * Moves the frog to a new position on the game board.
     * The frog moves randomly if its distance from the player snake's head is
     * greater than 5, otherwise, it moves towards the farthest position from all
     * snakes.
     */
    private void moveFrog() {
        int[] directions = { -1, 0, 1 };
        int maxDistance = Integer.MIN_VALUE;
        Tile bestMove = frog;
        if (calculateDistance(bestMove, snakes.get(0).head) > 5) {
            while (true) {
                int dx = directions[random.nextInt(3)];
                int dy = directions[random.nextInt(3)];
                if (dx != 0 || dy != 0) {
                    Tile newTile = new Tile(frog.x + dx, frog.y + dy);
                    if (isMoveSafe(newTile)) {
                        bestMove = newTile;
                        break;
                    }
                }
            }
        } else {
            for (int dx : directions) {
                for (int dy : directions) {
                    if (dx != 0 || dy != 0) {
                        Tile newTile = new Tile(frog.x + dx, frog.y + dy);
                        if (isMoveSafe(newTile)) {
                            int distance = 0;
                            for (Snake snake : snakes) {
                                distance += calculateDistance(newTile, snake.head);
                            }
                            if (distance > maxDistance) {
                                maxDistance = distance;
                                bestMove = newTile;
                            }
                        }
                    }
                }
            }
        }

        frog = bestMove;
    }

    /**
     * Calculates the Manhattan distance between two tiles on the game board.
     * 
     * @param tile1 The first tile.
     * @param tile2 The second tile.
     * @return The Manhattan distance between the two tiles.
     */
    private int calculateDistance(Tile tile1, Tile tile2) {
        return Math.abs(tile1.x - tile2.x) + Math.abs(tile1.y - tile2.y);
    }

    // AI Snake methods

    /**
     * Checks if a given tile is a safe move for a snake.
     * 
     * @param tile The tile to check.
     * @return True if the move is safe, false otherwise.
     */
    private boolean isMoveSafe(Tile tile) {
        // Check if the tile is within the game board boundaries
        if (tile.x < 0 || tile.x >= boardWidth / tileSize || tile.y < 0 || tile.y >= boardHeight / tileSize) {
            return false;
        }
        // Check for collisions with other snakes' heads and bodies
        for (Snake otherSnake : snakes) {
            if (collision(tile, otherSnake.head)) {
                return false;
            }
            for (Tile snakePart : otherSnake.body) {
                if (collision(tile, snakePart)) {
                    return false;
                }
            }
        }
        // Check for collisions with obstacles
        for (ArrayList<Tile> obstacle : obstacles) {
            for (Tile ob : obstacle) {
                if (collision(tile, ob)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Heuristic function to calculate the Manhattan distance between two tiles.
     * 
     * @param a The first tile.
     * @param b The second tile.
     * @return The Manhattan distance between the two tiles.
     */
    private int heur(Tile a, Tile b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Retrieves the possible moves for the snake based on its current velocity.
     * 
     * @param snake The snake for which to calculate possible moves.
     * @return A list of tiles representing the possible moves.
     */
    private List<Tile> getPossibleMoves(Snake snake) {
        List<Tile> PosibleMoves = new ArrayList<>();
        if (snake.velocityX == 1 && snake.velocityY == 0) {
            PosibleMoves.add(new Tile(snake.head.x + 1, snake.head.y));
            PosibleMoves.add(new Tile(snake.head.x, snake.head.y + 1));
            PosibleMoves.add(new Tile(snake.head.x, snake.head.y - 1));
        }
        if (snake.velocityY == 1 && snake.velocityX == 0) {
            PosibleMoves.add(new Tile(snake.head.x + 1, snake.head.y));
            PosibleMoves.add(new Tile(snake.head.x - 1, snake.head.y));
            PosibleMoves.add(new Tile(snake.head.x, snake.head.y + 1));
        }
        if (snake.velocityX == -1 && snake.velocityY == 0) {
            PosibleMoves.add(new Tile(snake.head.x - 1, snake.head.y));
            PosibleMoves.add(new Tile(snake.head.x, snake.head.y + 1));
            PosibleMoves.add(new Tile(snake.head.x, snake.head.y - 1));
        }
        if (snake.velocityY == -1 && snake.velocityX == 0) {
            PosibleMoves.add(new Tile(snake.head.x + 1, snake.head.y));
            PosibleMoves.add(new Tile(snake.head.x - 1, snake.head.y));
            PosibleMoves.add(new Tile(snake.head.x, snake.head.y - 1));
        }
        return PosibleMoves;
    }

    /**
     * Makes the next move for the specified snake based on the current game state.
     * 
     * @param snake The snake for which to determine the next move.
     */
    private void MakeNextMove(Snake snake) {
        List<Tile> possibleMoves = getPossibleMoves(snake);
        Tile bestMove = null;
        int minDistance = Integer.MAX_VALUE;

        for (Tile move : possibleMoves) {
            if (isMoveSafe(move)) {
                int distanceToApple = heur(move, apple);
                int distanceToFrog = heur(move, frog);
                if (distanceToApple < distanceToFrog) {
                    if (distanceToApple < minDistance) {
                        minDistance = distanceToApple;
                        bestMove = move;
                    }
                } else {
                    if (distanceToFrog < minDistance) {
                        minDistance = distanceToFrog;
                        bestMove = move;

                    }
                }
            }
        }
        if (bestMove == null) {

        } else if (bestMove.x - snake.head.x == 1) {
            snake.velocityX = 1;
            snake.velocityY = 0;
        } else if (bestMove.x - snake.head.x == -1) {
            snake.velocityX = -1;
            snake.velocityY = 0;
        } else if (bestMove.y - snake.head.y == 1) {
            snake.velocityY = 1;
            snake.velocityX = 0;
        } else if (bestMove.y - snake.head.y == -1) {
            snake.velocityY = -1;
            snake.velocityX = 0;
        }
    }

    /**
     * Handles actions performed by the game.
     * 
     * @param e The ActionEvent object representing the action performed.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
        if (snakes.get(0).isDead) {
            gameLoop.stop();
            saveScore(snakes.get(0).body.size());
            showGameOverDialog();

        }
    }

    /**
     * Handles key press events for controlling the player snake.
     * 
     * @param e The KeyEvent object representing the key press event.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && snakes.get(0).velocityY != 1) {
            snakes.get(0).velocityX = 0;
            snakes.get(0).velocityY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && snakes.get(0).velocityY != -1) {
            snakes.get(0).velocityX = 0;
            snakes.get(0).velocityY = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && snakes.get(0).velocityX != 1) {
            snakes.get(0).velocityX = -1;
            snakes.get(0).velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && snakes.get(0).velocityX != -1) {
            snakes.get(0).velocityX = 1;
            snakes.get(0).velocityY = 0;
        }
    }

    /**
     * This method is not needed and is left empty.
     * 
     * @param e The KeyEvent object representing the key typed event.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        // Do nothing
    }

    /**
     * This method is not needed and is left empty.
     * 
     * @param e The KeyEvent object representing the key typed event.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        // Do nothing
    }
}
