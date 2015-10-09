import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.JOptionPane;


@SuppressWarnings("serial")
public class SnakeCanvas extends Canvas implements Runnable, KeyListener
{
	private final int BOX_HEIGHT = 15;
	private final int BOX_WIDTH = 15;
	private final int GRID_WIDTH = 25;
	private final int GRID_HEIGHT = 25;
	
	private LinkedList<Point> snake = null;
	private Point fruit;
	private int direction = Direction.NO_DIRECTION;
	private int score = 0;
	private String highScore = "";
	
	private Thread runThread;
	
	private Image menuImage = null;
	private boolean isInMenu = true;
	
	private boolean isAtEndGame = false;
	private boolean won = false;
	
	public void paint(Graphics g)
	{
		if (runThread == null)
		{
			this.addKeyListener(this);
			runThread = new Thread(this);
			runThread.start();
		}
		
		if (isInMenu)
		{
			DrawMenu(g);
		}
		else if (isAtEndGame)
		{
			DrawEndGame(g);
		}
		else 
		{
			if (snake == null)
			{
				snake = new LinkedList<Point>();
				GenerateDefaultSnake();
				PlaceFruit();
			}
			
			if (highScore.equals(""))
			{
				highScore = this.GetHighScore();
			}
			
			DrawScore(g);
			DrawFruit(g);
			DrawGrid(g);
			DrawSnake(g);
		}
	}
	
	public void update(Graphics g)
	{
		//default update method
		Graphics offScreenGraphics;
		BufferedImage offScreen;
		Dimension d = this.getSize();
		
		offScreen = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		offScreenGraphics = offScreen.getGraphics();
		offScreenGraphics.setColor(this.getBackground());
		offScreenGraphics.fillRect(0, 0, d.width, d.height);
		offScreenGraphics.setColor(this.getForeground());
		paint(offScreenGraphics);
		
		//flip
		g.drawImage(offScreen, 0, 0, this);
	}
	
	public void GenerateDefaultSnake()
	{
		score = 0;
		snake.clear();
		
		snake.add(new Point(0, 2));
		snake.add(new Point(0, 1));
		snake.add(new Point(0, 0));
		
		direction = Direction.NO_DIRECTION;
	}
	
	public void Move()
	{
		if (direction == Direction.NO_DIRECTION)
			return;
		
		Point head = snake.peekFirst();
		Point newPoint = head;
		
		switch (direction)
		{
		case Direction.NORTH:
			newPoint = new Point(head.x, head.y - 1);
			break;
		case Direction.SOUTH:
			newPoint = new Point(head.x, head.y + 1);
			break;
		case Direction.WEST:
			newPoint = new Point(head.x - 1, head.y);
			break;
		case Direction.EAST:
			newPoint = new Point(head.x + 1, head.y);
			break;
		}
		
		snake.remove(snake.peekLast());
		
		if (newPoint.equals(fruit))
		{
			//snake has hit the fruit
			Point addPoint = (Point) newPoint.clone();
			
			switch (direction)
			{
			case Direction.NORTH:
				newPoint = new Point(head.x, head.y - 1);
				score += 10;
				break;
			case Direction.SOUTH:
				newPoint = new Point(head.x, head.y + 1);
				score += 10;
				break;
			case Direction.WEST:
				newPoint = new Point(head.x - 1, head.y);
				score += 10;
				break;
			case Direction.EAST:
				newPoint = new Point(head.x + 1, head.y);
				score += 10;
				break;
			}
			
			snake.push(addPoint);
			PlaceFruit();
		}
		else if ((newPoint.x < 0) || (newPoint.x > GRID_WIDTH - 1))
		{
			//go oob
			CheckScore();
			won = false;
			isAtEndGame = true;
			return;
		}
		else if ((newPoint.y < 0) || (newPoint.y > GRID_HEIGHT - 1))
		{
			//go oob
			CheckScore();
			won = false;
			isAtEndGame = true;
			return;
		}
		else if (snake.contains(newPoint))
		{
			//crashed into itself
			CheckScore();
			won = false;
			isAtEndGame = true;
			return;
		}
		else if (snake.size() == (GRID_WIDTH * GRID_HEIGHT))
		{
			CheckScore();
			won = true;
			isAtEndGame = true;
			return;
		}
		
		snake.push(newPoint);
	}
	
	public void DrawScore(Graphics g)
	{
		g.drawString("Score: " + score, 0, BOX_HEIGHT * GRID_HEIGHT + 10);
		g.drawString("Highscore: " + highScore, 0, BOX_HEIGHT * GRID_HEIGHT + 22);
	}
	
	public void CheckScore()
	{
		if (highScore.equals(""))
			return;
		else if (score > Integer.parseInt(highScore.split(":")[1]))
		{
			String name = JOptionPane.showInputDialog("Enter you name to save high score");
			highScore = name + ":" + score;
			
			File scoreFile = new File("highscore.dat");
			
			if (!scoreFile.exists())
			{
				try 
				{
					scoreFile.createNewFile();
				} catch (Exception e) 
				{
					
				}
			}
			
			FileWriter fileWrite = null;
			BufferedWriter writer = null;
			
			try
			{
				fileWrite = new FileWriter(scoreFile);
				writer = new BufferedWriter(fileWrite);
				writer.write(this.highScore);
			}catch (Exception e){e.printStackTrace();}
			finally
			{
				try
				{
					if (writer != null)
					{
						writer.close();
					}
				}catch (Exception e){e.printStackTrace();}
			}
				
		}
	}
	
	public void DrawEndGame(Graphics g)
	{
		BufferedImage endGame = new BufferedImage(this.getPreferredSize().width, this.getPreferredSize().height, BufferedImage.TYPE_INT_ARGB);
		Graphics endGameGraphics = endGame.getGraphics();
		endGameGraphics.setColor(Color.black);
		
		if (won)
			endGameGraphics.drawString("You Won!!!", this.getPreferredSize().width / 2, this.getPreferredSize().height / 2);
		else 
			endGameGraphics.drawString("You Lost", this.getPreferredSize().width / 2, this.getPreferredSize().height / 2);
		
		endGameGraphics.drawString("Your score is: " + this.score, this.getPreferredSize().width / 2, (this.getPreferredSize().height / 2) + 20);
		endGameGraphics.drawString("Press \"space\" to play again", this.getPreferredSize().width / 2, (this.getPreferredSize().height / 2) + 40);
		
		g.drawImage(endGame, 0,0, this);
	}
	
	public void DrawMenu(Graphics g)
	{
		if (this.menuImage == null)
		{
			try
			{
				URL imagePath = SnakeCanvas.class.getResource("snakemenu.png");
				menuImage = Toolkit.getDefaultToolkit().getImage(imagePath);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		g.drawImage(menuImage, 0, 0, 640, 480, this);
	}
	
	public void DrawGrid(Graphics g)
	{
		//drawing the outside rectangle
		g.drawRect(0, 0, GRID_WIDTH * BOX_WIDTH, GRID_HEIGHT * BOX_HEIGHT);
		
		//drawing the vertical lines
		for (int x = BOX_WIDTH; x < GRID_WIDTH * BOX_WIDTH; x += BOX_WIDTH)
		{
			g.drawLine(x, 0, x, BOX_HEIGHT * GRID_HEIGHT);
		}
					
		//drawing the horizontal lines
		for (int y = BOX_HEIGHT; y < BOX_HEIGHT * GRID_HEIGHT; y += BOX_HEIGHT)
		{
			g.drawLine(0, y, BOX_WIDTH * GRID_WIDTH, y);
		}
		
	}
	
	public void DrawSnake(Graphics g)
	{
		g.setColor(Color.GREEN);
		
		for (Point p : snake)
		{
			g.fillRect(p.x * BOX_WIDTH, p.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
		}
		
		g.setColor(Color.BLACK);
	}
	
	public void DrawFruit(Graphics g)
	{
		g.setColor(Color.RED);
		g.fillOval(fruit.x * BOX_WIDTH, fruit.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
		g.setColor(Color.BLACK);
	}
	
	public void PlaceFruit()
	{
		Random rand = new Random();
		int randomy = rand.nextInt(GRID_HEIGHT);
		int randomx = rand.nextInt(GRID_WIDTH);
		Point randomPoint = new Point(randomx, randomy);
		while(snake.contains(randomPoint))
		{
			randomx = rand.nextInt(GRID_WIDTH);
			randomy = rand.nextInt(GRID_HEIGHT);
			randomPoint = new Point(randomx, randomy);
		}
		fruit = randomPoint;
	}

	@Override
	public void run() {
		while (true)
		{
			if (!isInMenu && !isAtEndGame)
				Move();
			
			repaint();
			
			try
			{
				Thread.currentThread();
				Thread.sleep(100);
			}catch (Exception e) {e.printStackTrace();}
		}	
	}
	
	public String GetHighScore() 
	{
		FileReader readFile = null;
		BufferedReader filereader = null;
		
		try 
		{
			readFile = new FileReader("highscore.dat");
			filereader = new BufferedReader(readFile);
			return filereader.readLine();
		} 
		catch (Exception e) 
		{
			return "Nobody:0";
		}
		finally
		{
			try{if(filereader != null) filereader.close();}catch (Exception e) {e.printStackTrace();}
		}
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode())
		{
		case KeyEvent.VK_UP:
			if (direction != Direction.SOUTH)
				direction = Direction.NORTH;
			break;
			
		case KeyEvent.VK_DOWN:
			if (direction != Direction.NORTH)
				direction = Direction.SOUTH;
			break;
			
		case KeyEvent.VK_RIGHT:
			if (direction != Direction.WEST)
				direction = Direction.EAST;
			break;
			
		case KeyEvent.VK_LEFT:
			if (direction != Direction.EAST)
				direction = Direction.WEST;
			break;
			
		case KeyEvent.VK_ENTER:
			if (isInMenu)
			{
				isInMenu = false;
				repaint();
			}
			break;
			
		case KeyEvent.VK_ESCAPE:
			isInMenu = true;
			break;
		
		case KeyEvent.VK_SPACE:
			if (isAtEndGame)
			{
				isAtEndGame = false;
				won = false;
				GenerateDefaultSnake();
				repaint();
			}
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
