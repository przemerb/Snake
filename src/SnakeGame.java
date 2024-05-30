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

public class SnakeGame extends JPanel implements ActionListener, KeyListener{
    private class Tile {
        int x;
        int y;

        Tile(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
    
    class Node {
    	Tile tile;
    	Node parent;
    	int gCost, hCost, fCost;
    	
    	Node(Tile tile, Node parent, int gCost, int hCost){
    		this.tile = tile;
    		this.parent = parent;
    		this.gCost = gCost;
    		this.hCost = hCost;
    		this.fCost = gCost + hCost;
    	}
    }
    
    int boardWidth;
    int boardHeight;
    int tileSize = 25;

    //Snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;
    
    //AI1 Snake
    Tile aiSnakeHead;
    ArrayList<Tile> aiSnakeBody;
    
    //AI2 Snake
    Tile ai2SnakeHead;
    ArrayList<Tile> ai2SnakeBody;
    
    //Obstacles
    ArrayList<ArrayList<Tile>> obstacles;

    //Food
    Tile food;
    Random random;
    
    //Frog
    //Tile frog;
    //Thread frogThread;

    //game logic
    Timer gameLoop;
    int snakeVelocityX;
    int snakeVelocityY;
    int aiSnakeVelocityX;
    int aiSnakeVelocityY;
    int ai2SnakeVelocityX;
    int ai2SnakeVelocityY;
    boolean gameOver = false;
    boolean aiGameOver = false;
    boolean ai2GameOver = false;
    
    File scoreFile = new File("scores.txt");

    SnakeGame(int boardWidth, int boardHeight){
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<Tile>();
        
        aiSnakeHead = new Tile(10, 10);
        aiSnakeBody = new ArrayList<Tile>();
        
//        ai2SnakeHead = new Tile(40, 40);
//        ai2SnakeBody = new ArrayList<Tile>();
        
        random = new Random();
        
        //obstacles = new Tile[3][3];
        obstacles = new ArrayList<ArrayList<Tile>>();
        placeObstacles();

        food = new Tile(15,15);
        placeFood();
        
        //frog = new Tile(15, 15);
        //FrogThread();
        
        snakeVelocityX = 0;
        snakeVelocityY = 1;
        aiSnakeVelocityX = 0;
        aiSnakeVelocityY = -1;
//        ai2SnakeVelocityX = 1;
//        ai2SnakeVelocityY = 0;

        gameLoop = new Timer(200, this);
        gameLoop.start();
        
        Thread aiSnakeThread = new Thread(new Runnable() {
        	public void run() {
        		while(!aiGameOver) {
        			aiSnakeMove();
        			try {
        				Thread.sleep(200);
        			} catch (InterruptedException e) {
        				e.printStackTrace();
        			}
        			repaint();
        		}
        	}
        });
        
//        Thread ai2SnakeThread = new Thread(new Runnable() {
//        	public void run() {
//        		while(!ai2GameOver) {
//        			aiSnakeMove();
//        			try {
//        				Thread.sleep(200);
//        			} catch (InterruptedException e) {
//        				e.printStackTrace();
//        			}
//        			repaint();
//        		}
//        	}
//        });
        
        
        aiSnakeThread.start();
        //ai2SnakeThread.start();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        //Grid
        for(int i=0;i<boardWidth/tileSize; i++){
            g.drawLine(i*tileSize, 0, i*tileSize, boardHeight);
            g.drawLine(0, i*tileSize, boardWidth, i*tileSize);
        }
        //Food
        g.setColor(Color.red);
        g.fill3DRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, true);
        
        //Obstacle
        g.setColor(Color.gray);
        for (ArrayList<Tile> obstacle : obstacles) {
        	for (Tile tile : obstacle) {
        		g.fill3DRect(tile.x * tileSize, tile.y * tileSize, tileSize, tileSize, true);
        	}
        }
        
        //Snake Head
        g.setColor(Color.blue);
        g.fill3DRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, true);
        

        g.setColor(Color.green);
        //Snake body
        for (int i=0; i< snakeBody.size(); i++){
            Tile snakePart = snakeBody.get(i);
            g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
        }
        
        //AI1 Snake Head
        g.setColor(Color.green);
        g.fill3DRect(aiSnakeHead.x * tileSize, aiSnakeHead.y * tileSize, tileSize, tileSize, true);
        
        
        //AI1 Snake Body
        for (int i=0; i< aiSnakeBody.size(); i++) {
        	Tile snakePart = aiSnakeBody.get(i);
        	g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
        }
        
//        //AI2 Snake Head
//        g.setColor(Color.green);
//        g.fill3DRect(ai2SnakeHead.x * tileSize, ai2SnakeHead.y * tileSize, tileSize, tileSize, true);
//        
//        
//        //AI2 Snake Body
//        for (int i=0; i< ai2SnakeBody.size(); i++) {
//        	Tile snakePart = ai2SnakeBody.get(i);
//        	g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
//        }
        
        
        //Frog
        //g.setColor(Color.blue);
        //g.fill3DRect(frog.x * tileSize, frog.y * tileSize, tileSize, tileSize, true);

        //Score
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if(gameOver && aiGameOver){
            //g.setColor(Color.red);
            //g.drawString("Game over: "+ String.valueOf(snakeBody.size()), tileSize -16, tileSize);
        	showGameOverDialog();
        }
        else{
            g.drawString("Score: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
            g.drawString("ScoreAI: " + String.valueOf(aiSnakeBody.size()), tileSize - 16, tileSize);
        }
    }

    public void placeFood(){
    	boolean colides;
    	do {
    		food.x = random.nextInt(boardWidth/tileSize);
            food.y = random.nextInt(boardHeight/tileSize);
            colides = false;
            for(ArrayList<Tile> obstacle : obstacles) {
            	for(Tile tile : obstacle) {
            		if(collision(food, tile)) {
            			colides = true;
            			break;
            		}
            	}
            }
    	} while(colides);
//        food.x = random.nextInt(boardWidth/tileSize);
//        food.y = random.nextInt(boardHeight/tileSize);
    }
    
    public void placeObstacles() {
    	for (int i = 0; i < 3; ++i) {
    	    ArrayList<Tile> row = new ArrayList<>();
    	    obstacles.add(row);
    		int x = random.nextInt((boardWidth / tileSize) -6);
    		int y = random.nextInt((boardHeight / tileSize) -6);
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

    public boolean collision(Tile tile1, Tile tile2){
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    public void move(){
        //eat food
        if (collision(snakeHead, food)){
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
        }

        //Snake Body
        for (int i = snakeBody.size()-1; i >= 0; i--){
            Tile snakePart = snakeBody.get(i);
            if (i==0){
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            }
            else{
                Tile prevSnakePart = snakeBody.get(i-1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }

        //Snake Head
        snakeHead.x += snakeVelocityX;
        snakeHead.y += snakeVelocityY;

        //game over conditions
        for (int i=0; i < snakeBody.size(); i++){
            Tile snakePart = snakeBody.get(i);
            //collide with the snake head
            if (collision(snakeHead, snakePart)){
                gameOver = true;
            }
        }
        
        for (int i=0; i < aiSnakeBody.size(); i++){
            Tile aiSnakePart = aiSnakeBody.get(i);
            //collide with the snake head
            if (collision(snakeHead, aiSnakePart)){
                gameOver = true;
            }
        }
        
//        for (int i=0; i < ai2SnakeBody.size(); i++){
//            Tile ai2SnakePart = ai2SnakeBody.get(i);
//            //collide with the snake head
//            if (collision(snakeHead, ai2SnakePart)){
//                gameOver = true;
//            }
//        }

        if (snakeHead.x*tileSize < 0 || snakeHead.x*tileSize > boardWidth ||
            snakeHead.y*tileSize < 0 || snakeHead.y*tileSize > boardHeight){
                gameOver = true;
            }
        

        for (ArrayList<Tile> obstacle : obstacles) {
            for (Tile tile : obstacle) {
                if (collision(snakeHead, tile)) {
                    gameOver = true;
                }
            }
        }
        
        if(gameOver == true) {
    	    ArrayList<Tile> row = new ArrayList<>();
    	    obstacles.add(row);
    	    row.add(snakeHead);
            for (int i=0; i < snakeBody.size(); i++){
            	row.add(snakeBody.get(i));
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

        JLabel scoreLabel = new JLabel("Game Over! Your score: " + snakeBody.size());
        JLabel scoreLabelAI = new JLabel("AI1 score: " + aiSnakeBody.size());
        //JLabel scoreLabelAI2 = new JLabel("AI2 score: " + ai2SnakeBody.size());
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        gameOverDialog.add(scoreLabel, c);
        d.gridx = 0;
        d.gridy = 30;
        d.gridwidth = 2;
        gameOverDialog.add(scoreLabelAI, d);

        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
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
    
    private void restartGame() {
        snakeHead = new Tile(5, 5);
        snakeBody.clear();
        placeObstacles();
        placeFood();
        //frog = new Tile(15, 15);
        //FrogThread();
        snakeVelocityX = 0;
        snakeVelocityY = 1;
        aiSnakeVelocityX = 0;
        aiSnakeVelocityY = -1;
        //ai2SnakeVelocityX = 1;
        //aiSnakeVelocityY = 0;
        gameOver = false;
        aiGameOver = false;
        gameLoop.start();
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
    
//    private void FrogThread() {
//    	frogThread = new Thread(() -> {
//    		while(!gameOver) {
//    			moveFrog();
//    			try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//    			repaint();
//    		}
//    	});
//    	frogThread.start();
//    }
//    
//    private void moveFrog() {
//        int[] directions = {-1, 0, 1};
//        List<Tile> possibleMoves = new ArrayList<>();
//        int maxDistance = Integer.MIN_VALUE;
//        Tile bestMove = frog;
//
//        for (int dx : directions) {
//            for (int dy : directions) {
//                if (dx != 0 || dy != 0) {
//                    Tile newTile = new Tile(frog.x + dx, frog.y + dy);
//                    if (isSafeMove(newTile)) {
//                        int distance = calculateDistance(newTile, snakeHead);
//                        if (distance > maxDistance) {
//                            maxDistance = distance;
//                            bestMove = newTile;
//                        }
//                    }
//                }
//            }
//        }
//
//        frog = bestMove;
//    }
//
//    private boolean isSafeMove(Tile tile) {
//        if (tile.x < 0 || tile.x >= boardWidth / tileSize || tile.y < 0 || tile.y >= boardHeight / tileSize) {
//            return false;
//        }
//        if (collision(tile, snakeHead)) {
//            return false;
//        }
//        for (Tile snakePart : snakeBody) {
//            if (collision(tile, snakePart)) {
//                return false;
//            }
//        }
//        for (Tile[] obstacle : obstacles) {
//            for (Tile ob : obstacle) {
//                if (collision(tile, ob)) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    private int calculateDistance(Tile tile1, Tile tile2) {
//        return Math.abs(tile1.x - tile2.x) + Math.abs(tile1.y - tile2.y);
//    }
    
// AI Snake methods
    private boolean isMoveSafe(Tile tile) {
    	if (tile.x < 0 || tile.x >= boardWidth /tileSize || tile.y < 0 || tile.y >= boardHeight /tileSize) {
    		return false;
    	}
    	for (Tile snakePart : aiSnakeBody) {
    		if (collision(tile, snakePart)) {
    			return false;
    		}
    	}
    	for (ArrayList<Tile> obstacle : obstacles) {
    		for (Tile ob : obstacle) {
    			if(collision(tile, ob)) {
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    private int heur(Tile a, Tile b) {
    	return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
    
    private List<Tile> getPossibleMoves (Tile tile){
    	List<Tile> PosibleMoves = new ArrayList<>();
    	if(aiSnakeVelocityX == 1 && aiSnakeVelocityY == 0) {
    		PosibleMoves.add(new Tile(tile.x + 1, tile.y));
    		PosibleMoves.add(new Tile(tile.x, tile.y + 1));
    		PosibleMoves.add(new Tile(tile.x, tile.y - 1));
    	}
    	if(aiSnakeVelocityY == 1 && aiSnakeVelocityX == 0) {
    		PosibleMoves.add(new Tile(tile.x + 1, tile.y));
    		PosibleMoves.add(new Tile(tile.x - 1, tile.y));
    		PosibleMoves.add(new Tile(tile.x, tile.y + 1));
    	}
    	if(aiSnakeVelocityX == -1 && aiSnakeVelocityY == 0) {
    		PosibleMoves.add(new Tile(tile.x - 1, tile.y));
    		PosibleMoves.add(new Tile(tile.x, tile.y + 1));
    		PosibleMoves.add(new Tile(tile.x, tile.y - 1));
    	}
    	if(aiSnakeVelocityY == -1 && aiSnakeVelocityX == 0) {
    		PosibleMoves.add(new Tile(tile.x + 1, tile.y));
    		PosibleMoves.add(new Tile(tile.x - 1, tile.y));
    		PosibleMoves.add(new Tile(tile.x, tile.y - 1));
    	}
    	return PosibleMoves;
    }
    
    private void MakeNextMove() {
    	List<Tile> possibleMoves = getPossibleMoves(aiSnakeHead);
    	Tile bestMove = null;
    	int minDistance = Integer.MAX_VALUE;
    	
    	for (Tile move : possibleMoves) {
    		if (isMoveSafe(move)) {
    			int distance = heur(move, food);
    			if (distance < minDistance) {
    				minDistance = distance;
    				bestMove = move;
    			}
    		}
    	}
    	if (bestMove == null) {
    		
    	}
    	else if (bestMove.x - aiSnakeHead.x == 1) {
    		aiSnakeVelocityX = 1;
    		aiSnakeVelocityY = 0;
    	}
    	else if(bestMove.x - aiSnakeHead.x == -1) {
    		aiSnakeVelocityX = -1;
    		aiSnakeVelocityY = 0;
    	}
    	else if(bestMove.y - aiSnakeHead.y == 1) {
    		aiSnakeVelocityY = 1;
    		aiSnakeVelocityX = 0;
    	}
    	else if(bestMove.y - aiSnakeHead.y == -1) {
    		aiSnakeVelocityY = -1;
    		aiSnakeVelocityX = 0;
    	}
    	
    	return;
    }
    
	public void aiSnakeMove(){
		MakeNextMove();
		
	  //eat food
	  if (collision(aiSnakeHead, food)){
	      aiSnakeBody.add(new Tile(food.x, food.y));
	      placeFood();
	  }
	
	  //Snake Body
	  for (int i = aiSnakeBody.size()-1; i >= 0; i--){
	      Tile snakePart = aiSnakeBody.get(i);
	      if (i==0){
	          snakePart.x = aiSnakeHead.x;
	          snakePart.y = aiSnakeHead.y;
	      }
	      else{
	          Tile prevSnakePart = aiSnakeBody.get(i-1);
	          snakePart.x = prevSnakePart.x;
	          snakePart.y = prevSnakePart.y;
	      }
	  }
	
	  //Snake Head
	  aiSnakeHead.x += aiSnakeVelocityX;
	  aiSnakeHead.y += aiSnakeVelocityY;
	
	  //game over conditions
	  for (int i=0; i < aiSnakeBody.size(); i++){
	      Tile aiSnakePart = aiSnakeBody.get(i);
	      //collide with the snake head
	      if (collision(aiSnakeHead, aiSnakePart)){
	          aiGameOver = true;
	      }
	  }
	  
	  for (int i=0; i < snakeBody.size(); i++){
	      Tile snakePart = snakeBody.get(i);
	      //collide with the snake head
	      if (collision(aiSnakeHead, snakePart)){
	          aiGameOver = true;
	      }
	  }
	
	  if (aiSnakeHead.x*tileSize < 0 || aiSnakeHead.x*tileSize > boardWidth ||
	      aiSnakeHead.y*tileSize < 0 || aiSnakeHead.y*tileSize > boardHeight){
	          aiGameOver = true;
	      }
	  
	
	  for (ArrayList<Tile> obstacle : obstacles) {
	      for (Tile tile : obstacle) {
	          if (collision(aiSnakeHead, tile)) {
	              aiGameOver = true;
	          }
	      }
	  }
	  
      if(aiGameOver == true) {
  	    ArrayList<Tile> row = new ArrayList<>();
  	    obstacles.add(row);
  	    row.add(aiSnakeHead);
          for (int i=0; i < aiSnakeBody.size(); i++){
          	row.add(aiSnakeBody.get(i));
          }
      }
	}
    
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver){
            gameLoop.stop();
            saveScore(snakeBody.size());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
       if (e.getKeyCode() == KeyEvent.VK_UP && snakeVelocityY!=1){
        snakeVelocityX = 0;
        snakeVelocityY = -1;
       }
       else if (e.getKeyCode() == KeyEvent.VK_DOWN && snakeVelocityY!=-1){
        snakeVelocityX = 0;
        snakeVelocityY = 1;
       }
       else if (e.getKeyCode() == KeyEvent.VK_LEFT && snakeVelocityX!=1){
        snakeVelocityX = -1;
        snakeVelocityY = 0;
       }
       else if (e.getKeyCode() == KeyEvent.VK_RIGHT && snakeVelocityX!=-1){
        snakeVelocityX = 1;
        snakeVelocityY = 0;
       }
    }

    //do not need
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
