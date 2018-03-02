package connect4;

import java.util.concurrent.TimeUnit;

import javax.sound.midi.InvalidMidiDataException;

import launchpad.Launchpad;
import launchpad.LaunchpadMK2;
import launchpadGames.LaunchpadListener;

public class Connect4 {
	private int grid[];
	private int winningSlots[];
	public static Launchpad device;
	
	private boolean allowDrops = false;
	private boolean gameOn;
	private boolean quit;
	private boolean waiting;
	private int nextColor = 41;
	
	public void play() throws InterruptedException, InvalidMidiDataException {
		
		// Initialise variables
		nextColor = 41;
		quit = false;
		
		
		System.out.println("[OK] Setting up new game... ");
		System.out.print("[OK] Creating launchpad listener... ");
		Connect4.device.setReceiver(new LaunchpadListener() {
			@Override
			public void action(byte midi, byte vel) {
				if(vel >0) {	// note on
					if(device.toCoordinates(midi)[0] < 9) {
						if(allowDrops) {
							if(grid[Connect4.gridMap(device.toCoordinates(midi)[0], 8)] == 0) {
								new Thread() {
									public void run() {
										try {
											int[] coordinates = device.toCoordinates(midi);
											int color = nextColor;
											switchColor();
											if(drop(coordinates[0], color)) {
												allowDrops = false;
												gameOn = false;
											}
										} catch (Exception e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										
									}
									
								}.start();
							}
						}
					}else {
						switch(device.toCoordinates(midi)[1]) {
						case 4:	// Stop button
							quit = true;
							System.out.println("QUITTING");
							break;
						}
					}
					
					if(waiting) {
						waiting = false;
					}
				}
			}
			
		});
		
		
		while(!quit) {
			System.out.print("[DONE]\n[OK] Initialising slots... ");
			grid = new int[64];
			// set all to 0
			for(int i=0; i<64; i++) {
				grid[i] = 0;
			}
			winningSlots = new int[4];

			System.out.print("[DONE]\n[OK] Clearing screen... ");
			
			device.clearScreen();
			gameOn = true;
			allowDrops = true;
			
			System.out.print("[DONE]\n[OK] Game Start!\n");
			
			this.switchColor();
			this.switchColor(); // twice so that the loser goes first
			
			
			boolean noMoreSlots = false;
			
			while(gameOn && !noMoreSlots && !quit) {
				// Wait for game to end
				TimeUnit.MILLISECONDS.sleep(100);
				for(int i=56; i<64; i++) {
					if(grid[i]==0) {
						noMoreSlots = false;
						break;
					}else noMoreSlots = true;
				}
			}
			
			this.allowDrops = false;
			
			if(!noMoreSlots && !quit) {	// if game ended due to victory...
				// Victory display
				int counter = 0;
				this.waiting = true;
				while(this.waiting) {
					if(counter < 5) {
						for(int i : winningSlots) {
							device.send(i, 3);
						}
					}else {
						for(int i : winningSlots) {
							device.send(i, grid[Connect4.gridMap(device.toCoordinates(i))]);
						}
					}
					counter++;
					counter %=10;
					TimeUnit.MILLISECONDS.sleep(30);
				}
			}
		} // End of while(!quit) loop
		
		Connect4.device.clearScreen();
	}
	
	
	
	
	
	public boolean drop(int collumn, int color) throws InvalidMidiDataException, InterruptedException {
		// Flash collumn
		Connect4.device.fill(collumn, 1, collumn, 8, 1);
		TimeUnit.MILLISECONDS.sleep(50);
		this.drawSlots(collumn, 1, collumn, 8);
		
		int y = 8;
		int delay = 50;
		// Begin drop
		while((grid[Connect4.gridMap(collumn, y-1)] == 0)&&(gameOn)) {
			Connect4.device.send(device.toMidi(collumn, y), color);
			TimeUnit.MILLISECONDS.sleep(delay);
			Connect4.device.send(device.toMidi(collumn, y), 0);
			delay *= 0.75;
			y--;
			// check if at bottom
			if(y==1) {
				break;
			}
		}
		if(gameOn) {
			// Impact Flash
			Connect4.device.send(device.toMidi(collumn, y), 3);
			TimeUnit.MILLISECONDS.sleep(70);
			// Update and draw slot
			grid[Connect4.gridMap(collumn, y)] = color;
		}
		this.drawSlot(collumn,  y);
		
		if(gameOn) {
			return victoryCheck(collumn, y);
		}else return false;
	}
	
	private void switchColor() throws InvalidMidiDataException {
		if(nextColor == 41) {
			nextColor = 53;
		}else {
			nextColor = 41;
		}
		
		// Draw
		for(int i=0; i<8; i++) {
			device.send(104+i, nextColor);
		}
	}
	
	private void drawSlots(int x1, int y1, int x2, int y2) throws InvalidMidiDataException {
		for(int j = y1; j<=y2; j++) {
			for(int i=x1; i<=x2; i++) {
				Connect4.device.send(Connect4.device.toMidi(i, j), grid[Connect4.gridMap(i, j)]);
			}
		}
	}
	private  void drawSlot(int x, int y) throws InvalidMidiDataException {
		Connect4.device.send(Connect4.device.toMidi(x, y), grid[Connect4.gridMap(x, y)]);
	}
	
	
	private static int gridMap(int x, int y) {
		return y*8 + x - 9;
	}
	
	private static int gridMap(int[] xy) {
		if(xy.length < 2) {
			return -1;
		}
		
		return xy[1]*8 + xy[0] - 9;
	}
	
	private boolean victoryCheck(int x, int y) {
		int temp = 1;
		int counter = 0;
		
		// Horizontal check
		for(int i = 1; i<9; i++) {
			if((grid[Connect4.gridMap(i, y)] != temp)) {
				if(grid[Connect4.gridMap(i, y)] != 0) {
					temp = grid[Connect4.gridMap(i, y)];
					counter = 1;
					winningSlots[0] = device.toMidi(i, y);
				}
			}else {
				winningSlots[counter] = device.toMidi(i, y);
				counter++;
				if(counter >= 4) {
					return true;
				}
			}
			
		}
		
		// Vertical Check
		counter = 0;
		for(int i = 1; i<9; i++) {
			
			if(grid[Connect4.gridMap(x, i)] != temp) {
				if(grid[Connect4.gridMap(x, i)] != 0) {
					temp = grid[Connect4.gridMap(x, i)];
					counter = 1;
					winningSlots[0] = device.toMidi(x, i);
				}
			}else {
				winningSlots[counter] = device.toMidi(x, i);
				counter++;
				if(counter >= 4) {
					return true;
				}
			}
		}
		
		// Diagonal Check 1 (upper right)
		counter = 0;
		for(int i = -(Math.min(x, y) - 1); i<=8-Math.max(x, y); i++) {
			if(grid[Connect4.gridMap(x+i, y+i)] != temp) {
				if(grid[Connect4.gridMap(x+i, y+i)] != 0) {
					temp = grid[Connect4.gridMap(x+i, y+i)];
					counter = 1;
					winningSlots[0] = device.toMidi(x+i, y+i);
				}
			}else {
				winningSlots[counter] = device.toMidi(x+i, y+i);
				counter++;
				if(counter >= 4) {
					return true;
				}
			}
		}
		// Diagonal Check 2 (lower right)
		counter = 0;
		
		int tempi, tempj;
		if(y>9-x) {
			tempi = 8-y;
			tempj = 8-x;
		}else {
			tempi = x-1;
			tempj = y-1;
		}
		System.out.println("checking from "+x + ", " + y + " | -"+tempi+" to +"+tempj);
		for(int i = -tempi; i<=tempj; i++) {
			if(grid[Connect4.gridMap(x+i, y-i)] != temp) {		// if it's different
				if(grid[Connect4.gridMap(x+i, y-i)] != 0) {		// if not black
					temp = grid[Connect4.gridMap(x+i, y-i)];	// update temp
					counter = 1;
					winningSlots[0] = device.toMidi(x+i, y-i);
				}
			}else {
				winningSlots[counter] = device.toMidi(x+i, y-i);
				counter++;
				if(counter >= 4) {
					System.out.println(temp);
					return true;
				}
			}
		}
		return false;
	}
	
	public void printGrid() {
		for(int i=8; i>0; i--) {
			for(int j=1; j<9; j++) {
				System.out.print(grid[Connect4.gridMap(j, i)]+"  ");
			}
			System.out.print("\n");
		}
	}
	
	public static void setDevice(Launchpad device) {
		Connect4.device = device;
	}
	
	public static void main(String args[]) {
		Connect4 game = new Connect4();
		try {
			Connect4.setDevice(new LaunchpadMK2());
			
			game.play();
			
			Connect4.device.kill();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
