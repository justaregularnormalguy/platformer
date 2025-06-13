package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.awt.Toolkit;
import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();
	private ArrayList<Water> waters = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;
	private long timer;
	private long timeToBeat;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
		timeToBeat = 45000;
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];
		waters = new ArrayList<>();
		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18){
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
					waters.add((Water)tiles[x][y]);
				}
				else if (values[x][y] == 19){
					waters.add((Water)tiles[x][y]);
				
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);}
				else if (values[x][y] == 20){
					waters.add((Water)tiles[x][y]);
				
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);}
				else if (values[x][y] == 21){
					waters.add((Water)tiles[x][y]);
				
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);}
			}

		}	
		timer = System.currentTimeMillis();
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		
		if (active) {
			if (System.currentTimeMillis() - timer > timeToBeat ) {
				onPlayerDeath();
			}
			// Update the player
			player.update(tslf);

			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if(flowers.get(i).getType() == 1){
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
						//precondition: the player touches a flower
						//postcondition: a beep sound is played
						Toolkit.getDefaultToolkit().beep();
					}
					else
						addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			} 
			boolean touchingWater = false;
			for(Water w: waters){
				if(w.getHitbox().isIntersecting(player.getHitbox())){
					System.out.println("Touching water");
					touchingWater = true;
				}
			}
			if (touchingWater != true){
				System.out.println("Not touching water");
			}                                                                    
			
			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			//Precondiition The player's x position is greater than 25
			// Postcondition: If the player is at or below x position 25, the player loses
			if (player.getX() <= 25 ) {
				onPlayerDeath();
				 
				 
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}
	
	// Precondition: col and row are within map bounds; map is not null; numSquaresToFill > 0; placedThisRound is not null; target tile is not solid or is already Gas
	// Postcondition: Up to numSquaresToFill non-solid, non-gas tiles (including the start) are filled with Gas tiles; map and placedThisRound are updated accordingly
    
    private void addGas(int col, int row, Map map, int numSquaresToFill, ArrayList<Gas> placedThisRound) {
        // Creates and places the initial Gas tile
        Gas g = new Gas (col, row, tileSize, tileset.getImage("GasOne"), this, 0);
        map.addTile(col, row, g);
        numSquaresToFill--;
        placedThisRound.add(g);

        // Spreads gas to adjacent tiles until the limit is reached
        while(numSquaresToFill > 0 && placedThisRound.size() > 0){

            col = placedThisRound.get(0).getCol();
            row = placedThisRound.get(0).getRow();
            placedThisRound.remove(0);

            // Check all adjacent positions
            for(int rowIndex = row-1; rowIndex < row+2; rowIndex++){
                for(int colIndex = col; colIndex > col-2 ; colIndex -= 2)
                {
                    // Only spreads to in-bounds, non-solid, non-gas tiles
                    if (colIndex >= 0 && colIndex < map.getTiles().length && rowIndex >= 0 && rowIndex < map.getTiles()[0].length ){
                        if(!(map.getTiles()[colIndex][rowIndex].isSolid()) && !(map.getTiles()[colIndex][rowIndex] instanceof Gas) && !(map.getTiles()[colIndex][rowIndex] instanceof Flag) && numSquaresToFill > 0 ){
                            // Places new Gas tile and continues spreading
                            g = new Gas (colIndex, rowIndex, tileSize, tileset.getImage("GasOne"), this, 0);
                            map.addTile(colIndex, rowIndex, g);
                            numSquaresToFill--;
                            placedThisRound.add(g);	
                        }
                    }

                    
                    if(colIndex == col){
                        colIndex += 3;
                    }
                }   
            } 
        }
    }	
	//#############################################################################################################
	//Your code goes here! 
	//Please make sure you read the rubric/directions carefully and implement the solution recursively!
	private void water(int col, int row, Map map, int fullness) {
		//Falling_water
		//Full_water
		//Half_water
		//Quarter_water

		String typeOfWater = "Full_water";
		if(fullness == 2)
		typeOfWater = "Half_water";
		else if(fullness == 1)
		typeOfWater = "Quarter_water";
				else if(fullness == 0)
		typeOfWater = "Falling_water";
		//make water (You’ll need modify this to make different kinds of water such as half water and quarter water)
		Water w = new Water (col, row, tileSize, tileset.getImage(typeOfWater), this, fullness);
		map.addTile(col, row, w);
		waters.add(w);

                       //check if we can go down

	 	if(row + 1 < map.getTiles()[0].length && !(map.getTiles()[col][row + 1].isSolid())){
		
			if(row + 2 <map.getTiles()[col].length && map.getTiles()[col][row + 2].isSolid()){
			water(col, row + 1, map, 3);
			}
			else{
			water(col, row + 1, map, 0);
			}
		}
                       //if we can’t go down go left and right.
		//right
		else if(row + 1 < map.getTiles()[0].length){
			if (fullness > 1){fullness  =fullness -1; }
		if(col+1 < map.getTiles().length && !(map.getTiles()[col+1][row] instanceof Water)&& !(map.getTiles()[col+1][row ].isSolid())) {

			water(col+1, row, map, fullness);
		}
		//left
		if(col-1 >= 0 && !(map.getTiles()[col-1][row] instanceof Water) && !(map.getTiles()[col-1][row].isSolid())) {
			water(col-1, row, map, fullness);
		}
	}
	}



	public void draw(Graphics g) {
	   	 g.translate((int) -camera.getX(), (int) -camera.getY());
	   	 // Draw the map
	   	 for (int x = 0; x < map.getWidth(); x++) {
	   		 for (int y = 0; y < map.getHeight(); y++) {
	   			 Tile tile = map.getTiles()[x][y];
	   			 if (tile == null)
	   				 continue;
	   			 if(tile instanceof Gas) {
	   				// Adjust gas intensity and image based on number of adjacent Gas tiles
       				 int adjacencyCount =0;
       				 for(int i=-1; i<2; i++) {
       					 for(int j =-1; j<2; j++) {
       						 if(j!=0 || i!=0) {
       							 if((x+i)>=0 && (x+i)<map.getTiles().length && (y+j)>=0 && (y+j)<map.getTiles()[x].length) {
       								 if(map.getTiles()[x+i][y+j] instanceof Gas) {
       									 adjacencyCount++;
       								 }
       							 }
       						 }
       					 }
       				 }
       				 // Set intensity and image based on adjacency
       				 if(adjacencyCount == 8) {
       					 ((Gas)(tile)).setIntensity(2);
       					 tile.setImage(tileset.getImage("GasThree"));
       				 }
       				 else if(adjacencyCount >5) {
       					 ((Gas)(tile)).setIntensity(1);
       					tile.setImage(tileset.getImage("GasTwo"));
       				 }
       				 else {
       					 ((Gas)(tile)).setIntensity(0);
       					tile.setImage(tileset.getImage("GasOne"));
       				 }
       			 }
	   			 if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
	   				 tile.draw(g);
	   		 }
	   	 }


	   	 // Draw the enemies
	   	 for (int i = 0; i < enemies.length; i++) {
	   		 enemies[i].draw(g);
	   	 }


	   	 // Draw the player
	   	 player.draw(g);
		 //precondition: game starts
		 //postcondition: if the timer elapses, the game loses
		 g.drawString((timeToBeat - (System.currentTimeMillis() - timer)) /1000 + "", (int) (player.getX()+tileSize/2), (int) (player.getY()+tileSize/2));



	   	 // used for debugging
	   	 if (Camera.SHOW_CAMERA)
	   		 camera.draw(g);
	   	 g.translate((int) +camera.getX(), (int) +camera.getY());

		 
	    }


	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}
	



}
