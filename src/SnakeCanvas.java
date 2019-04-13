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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JOptionPane;


public class SnakeCanvas extends Canvas implements Runnable , KeyListener
{
	private final int BOX_HEIGHT = 15;
	private final int BOX_WIDTH = 15;
	private final int GRID_WIDTH = 25;
	private final int GRID_HEIGTH = 25;
	
	private LinkedList<Point> snake;
	private Point fruit;
	private int direction = Direction.NO_DIRECTION;
	
	private Thread runThread;
	//private Graphics globalGraphics;
	private int score = 0;
	private String highscore ="";
	
	private Image menuImage = null;
	private boolean isInMenu = true;
	
	public void paint(Graphics g)
	{
		if(runThread == null)
		{
			this.setPreferredSize(new Dimension(640 , 480));
			this.addKeyListener(this);
			runThread = new Thread(this);
			runThread.start();
		}
		if(isInMenu){
			//draw menu
			DrawMenu(g);
			
		}else{
			//draw everything else
			if(snake == null){
				snake = new LinkedList<Point>();
				GenerateDefaultSnake();
				PlaceFruit();
				//fruit = new Point(10 , 10);
			}
			if(highscore.equals("")){
				//init the highscore
				highscore = this.GetHighScore();
				System.out.println(highscore);
			}
			DrawFurit(g);
			DrawGrid(g);
			DrawSnake(g);
			DrawScore(g);
		}
	}
	
	public void DrawMenu(Graphics g)
	{
		if(this.menuImage == null){
			try{
				URL imagePath = SnakeCanvas.class.getResource("snakeMenu.png");
				menuImage = Toolkit.getDefaultToolkit().getImage(imagePath);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		g.drawImage(menuImage, 0,0, 640,480,this);
	}
	
	public void update(Graphics g)
	{
		//this is the default update method which will contain our double buffering
		Graphics offScreenGraphics; // these are the graphics we will use to draw offscreen
		BufferedImage offScreen = null;
		Dimension d = this.getSize();
		
		offScreen  = new BufferedImage(d.width, d.height,BufferedImage.TYPE_INT_ARGB);
		offScreenGraphics = offScreen.getGraphics();
		offScreenGraphics.setColor(this.getBackground());
		offScreenGraphics.fillRect(0, 0, d.width, d.height);
		offScreenGraphics.setColor(this.getForeground());
		paint(offScreenGraphics);
		
		//flip
		g.drawImage(offScreen, 0, 0,this);
	}
	
	public void GenerateDefaultSnake(){
		score = 0;
		snake.clear();
		snake.add(new Point(0 , 2));
		snake.add(new Point(0 , 1));
		snake.add(new Point(0 , 0));
		direction = Direction.NO_DIRECTION;
	}
	
	public void Move()
	{
		Point head = snake.peekFirst();
		Point newPoint = head;
		switch (direction) {
		case Direction.NORTH:
			newPoint = new Point(head.x , head.y - 1);
			break;
		case Direction.SOUTH:
			newPoint = new Point(head.x , head.y + 1);
			break;
		case Direction.WEST:
			newPoint = new Point(head.x - 1 , head.y);
			break;	
		case Direction.EAST:
			newPoint = new Point(head.x + 1 , head.y);
			break;
		}
		
		snake.remove(snake.peekLast());
		
		if(newPoint.equals(fruit))
		{
			// the snake has hit fruit
			score +=10;
			Point addPoint = (Point)newPoint.clone();
			switch (direction) {
			case Direction.NORTH:
				newPoint = new Point(head.x , head.y - 1);
				break;
			case Direction.SOUTH:
				newPoint = new Point(head.x , head.y + 1);
				break;
			case Direction.WEST:
				newPoint = new Point(head.x - 1 , head.y);
				break;	
			case Direction.EAST:
				newPoint = new Point(head.x + 1 , head.y);
				break;
			}
			snake.push(addPoint);
			PlaceFruit();
			
		}
		else if(newPoint.x < 0 || newPoint.x > GRID_WIDTH - 1)
		{
			// we want OOB(out of bound) , reset game
			//CheckScore();
			GenerateDefaultSnake();
			return;
		}
		else if(newPoint.y < 0 || newPoint.y > GRID_HEIGTH - 1)
		{
			//we want OOB(out of bound) , reset game
			//CheckScore();
			GenerateDefaultSnake();
			return;
		}
		else if(snake.contains(newPoint))
		{
			// we ran into ourselves , reset game
			//CheckScore();
			GenerateDefaultSnake();
			return;
		}
		
		//if we reach this point in code , we're still good
		snake.push(newPoint);
	}
	
	public void DrawScore(Graphics g)
	{
		 g.drawString("Score: "+ score, 0,BOX_HEIGHT * GRID_HEIGTH + 10);
		 g.drawString("HighScore: "+ highscore , 0,BOX_HEIGHT * GRID_HEIGTH + 20);
	}
	
	public void CheckScore()
	{
		System.out.println(highscore);
		// format Bardon/:/100
		if(score > Integer.parseInt(highscore.split(":")[1]))
		{
			//user  has set a new record
			String name = JOptionPane.showInputDialog("You set a new highscore. What is your name?");
			highscore = name + " : " + score;
			
			File scoreFile = new File("highscore.dat");
			if(!scoreFile.exists())
			{
				try {
					scoreFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileWriter writeFile = null;
			BufferedWriter writer = null;
			
			try{
				writeFile = new FileWriter(scoreFile);
				writer = new BufferedWriter(writeFile);
				writer.write(this.highscore);
			}catch(Exception e){
				
			}
			finally
			{
				try {
					if(writer != null)
					writer.close();
				} catch (Exception e) {}
			}
		}
	}
	public void DrawGrid(Graphics g)
	{
		//drawing an outside rect
		g.drawRect(0,0, GRID_WIDTH * BOX_WIDTH, GRID_HEIGTH * BOX_HEIGHT);
		//drawing the vertical lines
		for(int x = BOX_WIDTH;x < GRID_WIDTH * BOX_WIDTH ; x+= BOX_WIDTH){
			g.drawLine(x,0, x, BOX_HEIGHT * GRID_HEIGTH);
		}
		//drawing the horizontal lines
		for(int y = BOX_HEIGHT;y < GRID_HEIGTH* BOX_HEIGHT; y+= BOX_HEIGHT){
			g.drawLine(0, y, GRID_WIDTH * BOX_HEIGHT , y );
		}
	}
	
	public void DrawSnake(Graphics g)
	{
		g.setColor(Color.GREEN);
		for(Point p : snake){
			g.fillRect(p.x * BOX_WIDTH ,p.y * BOX_HEIGHT, BOX_WIDTH,BOX_HEIGHT);
		}
		g.setColor(Color.BLACK);
	}
	
	public void DrawFurit(Graphics g)
	{
		g.setColor(Color.RED);
		g.fillOval(fruit.x * BOX_WIDTH,fruit.y * BOX_HEIGHT, BOX_WIDTH,BOX_HEIGHT);
		g.setColor(Color.BLACK);
	}

	public void PlaceFruit()
	{
		// generate random place for fruit
		Random rand = new Random();
		int randomX =rand.nextInt(GRID_WIDTH);
		int randomY = rand.nextInt(GRID_HEIGTH);
		Point randomPoint = new Point(randomX , randomY);
		while(snake.contains(randomPoint))
		{
			randomX = rand.nextInt(GRID_WIDTH);
			randomY = rand.nextInt(GRID_HEIGTH);
			randomPoint = new Point(randomX, randomY);
		}
		fruit = randomPoint;
	}
	
	@Override
	public void run() {
		while(true)
		{
			// runs indefinitely
			repaint();
			if(!isInMenu)
				Move();
			
			try{
				Thread.currentThread();
				Thread.sleep(100);
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public String GetHighScore()
	{
		//format: Brandon:100
		FileReader readFile = null;
		BufferedReader reader =  null;
		try {
			readFile = new FileReader("highscore.dat");
			reader = new BufferedReader(readFile);
			return reader.readLine();
		} catch (Exception e) {
			return "Nobody:0";
		}
		finally{
			try {
				if(reader!=null)
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			if(direction != Direction.SOUTH)
				direction = Direction.NORTH;
			break;
		case KeyEvent.VK_DOWN:
			if(direction != Direction.NORTH)
				direction = Direction.SOUTH;
			break;
		case KeyEvent.VK_RIGHT:
			if(direction != Direction.WEST)
				direction = Direction.EAST;
			break;
		case KeyEvent.VK_LEFT:
			if(direction != Direction.EAST)
				direction = Direction.WEST;
			break;
		case KeyEvent.VK_ENTER:
			if(isInMenu)
			{
				isInMenu = false;
				repaint();
			}
			break;
		case KeyEvent.VK_ESCAPE:
			isInMenu = true;
			break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
