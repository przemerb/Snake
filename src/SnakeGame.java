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

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    int boardWidth;
    int boardHeight;
    int tileSize = 20;

    ArrayList<Snake> snakes;
    ArrayList<Thread> snakeThreads;

    // Obstacles
    ArrayList<ArrayList<Tile>> obstacles;

    // Apple
    Tile apple;
    Random random;

    // Frog
    Tile frog;
    Thread frogThread;
    boolean eatenFrog = false;

    // game logic
    Timer gameLoop;

    File scoreFile = new File("scores.txt");

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

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

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

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

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

        // Check collision with other snakes
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

        if (snake.head.x * tileSize < 0 || snake.head.x * tileSize > boardWidth ||
                snake.head.y * tileSize < 0 || snake.head.y * tileSize > boardHeight) {
            snake.isDead = true;
        }

        for (ArrayList<Tile> obstacle : obstacles) {
            for (Tile tile : obstacle) {
                if (collision(snake.head, tile)) {
                    snake.isDead = true;
                }
            }
        }
    }

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

    private void saveScore(int score) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile, true))) {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println(score + " " + date);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private int calculateDistance(Tile tile1, Tile tile2) {
        return Math.abs(tile1.x - tile2.x) + Math.abs(tile1.y - tile2.y);
    }

    // AI Snake methods
    private boolean isMoveSafe(Tile tile) {
        if (tile.x < 0 || tile.x >= boardWidth / tileSize || tile.y < 0 || tile.y >= boardHeight / tileSize) {
            return false;
        }
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
        for (ArrayList<Tile> obstacle : obstacles) {
            for (Tile ob : obstacle) {
                if (collision(tile, ob)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int heur(Tile a, Tile b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

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

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
        if (snakes.get(0).isDead) {
            gameLoop.stop();
            saveScore(snakes.get(0).body.size());
            showGameOverDialog();
            
        }
    }

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

    // do not need
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
