package reflex;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.InvalidMidiDataException;

import launchpad.Coordinate;
import launchpad.LaunchpadMK2;
import launchpad.Launchpad;
import launchpad.LaunchpadListener;

public class Reflex {
	public static final int MISSILE_LAUNCH_VEL = 30000;
	public static final int MISSILE_ACC = 400000;
	public static boolean gameOn = false;
	public static float speed = 1;
	
	public static enum STATES {ACTIVE, INVINCIBLE, PASSIVE};
	public static int bombCounterMod;
	public static int activeBombs;
	
	public static Launchpad device;
	
	// IN-GAME STUFF
	private ArrayList<Bomb> bombs;
	private int redTeamLives = 4;
	private int blueTeamLives = 4;
	
	private int redTeamColors[] = {53, 54, 55, 54};
	private int blueTeamColors[] = {41, 42, 43, 42};
	private int colorCounter = 0;
	
	private boolean missileEn = false;
	
	private Coordinate bombDetonationLoc;
	private boolean nextKeyPress;
	
	
	public Reflex(){
		// Startup launchpad
		System.out.print("[OK] Setting up variables... ");
		bombs = new ArrayList<Bomb>(8);
		System.out.print("[DONE]\n");
	}
	
	public void kill() {
		Reflex.gameOn = false;
		this.missileEn = false;
		Reflex.device.kill();
	}
	
	public void setDevice(LaunchpadMK2 device) {
		System.out.print("[OK] Setting device... ");
		Reflex.device = device;
		System.out.print("[DONE]\n");
	}
	
	public void play() throws InvalidMidiDataException, InterruptedException {
		if(Reflex.device == null) {
			System.out.println("[ER] Please set a midi device using 'Reflex.setDevice(Launchpad device);' !");
		}else {
			System.out.print("[DONE]\n[OK] Initialising... \n");
			System.out.print("[OK] Setting device receiver... ");
			Reflex.device.setReceiver(new LaunchpadListener() {
				@Override
				public void action(byte midi, byte vel) {
					if(vel != 0){ 		// When button pushed
						// MISSILE LAUNCHES
						if(missileEn){	
							if((Reflex.device.toCoordinate(midi).y == 8) || (Reflex.device.toCoordinate(midi).y == 1)) {
								new Missile(Reflex.device.toCoordinate(midi)) {
									Bomb b = bombs.get(this.loc.x-1);
									@Override
									public boolean checkCollision(int dir) throws InvalidMidiDataException {
										
										if(this.loc.y == b.loc.y) {
											// Impact!
											b.impact(dir);
											return true;
										}else return false;
									}
								};
							}
						}
						nextKeyPress = true;
					}
				}
			});
			System.out.print("[DONE]\n[OK] Starting!\n");
			
			while(true) {
				System.out.println("[OK] Starting new game!");
				while(true) {
					System.out.print("[OK] Setting up new round");
					
					// New Game Setup
					Reflex.gameOn = true;
					Reflex.device.clearScreen();
					
					System.out.print(".");
					
					// Draw Lives
					for(int i = 0; i < redTeamLives; i++) {
						Reflex.device.send(Reflex.device.toMidi(9, 8-i), redTeamColors[0]);
					}
					for(int i = 0; i < blueTeamLives; i++) {
						Reflex.device.send(Reflex.device.toMidi(9, 1+i), blueTeamColors[0]);
					}
					
					System.out.print(".");
					
					// Start bombs
					for(int i = 1; i < 9; i++) {
						bombs.add(new Bomb(i){
							@Override
							public void detonate(Coordinate c) {
								if(c.y > 4) {
									redTeamLives--;
								}else blueTeamLives--;
								bombDetonationLoc = c;
								Reflex.gameOn = false;
							}
						});
					}
				
					System.out.print(". [DONE]\n[OK] Game start!\n");
					missileEn = true;
					try {
						// Barricade drawing while in game
						while (Reflex.gameOn) {
							for(int i=1; i<9; i++) {
								Reflex.device.send(Reflex.device.toMidi(i, 1), blueTeamColors[colorCounter]);
								Reflex.device.send(Reflex.device.toMidi(i, 8), redTeamColors[colorCounter]);
								colorCounter++;
								colorCounter %= 4;
							}
							colorCounter++;
							colorCounter %= 4;
							TimeUnit.MILLISECONDS.sleep(100);
						}
						
						
						// Explosion graphics
						// Draw flash from last detonation line
						if(this.bombDetonationLoc.y>4) {
							Reflex.device.fill(1, 7,  8, 7, 3);
							Reflex.device.fill(1, 6,  8, 6, 2);	
						}else {
							Reflex.device.fill(1, 2,  8, 2, 3);
							Reflex.device.fill(1, 3,  8, 3, 2);
						}
						Reflex.device.fill(1, 5,  8, 5, 1);
						Reflex.device.fill(1, 4,  8, 4, 1);
						
						TimeUnit.MILLISECONDS.sleep(70);
						Reflex.device.fill(1, 2,  9, 7, 0);
						Reflex.device.send(Reflex.device.toMidi(9, 1), 0);
						Reflex.device.send(Reflex.device.toMidi(9, 8), 0);
						
						
						// Update score board
						for(int i = 0; i < redTeamLives; i++) {
							Reflex.device.send(Reflex.device.toMidi(9, 8-i), redTeamColors[0]);
						}
						for(int i = 0; i < blueTeamLives; i++) {
							Reflex.device.send(Reflex.device.toMidi(9, 1+i), blueTeamColors[0]);
						}
						
						System.out.println("[OK] Round over! Score: R=" + redTeamLives + " | B=" + blueTeamLives);
						
					}catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					Reflex.gameOn = false;	// Force global signal to stop (should've done already)
					missileEn = false;		// Stop missile launches
					
					// Wait for bombs to die, then dispose
					for(Bomb b : bombs) {
						b.join();
						Reflex.device.send(Reflex.device.toMidi(b.loc), 0);
					}
					bombs.clear();			// Empty array (java should release resources)
					
					// Display the hole in the wall
					Reflex.device.send(Reflex.device.toMidi(bombDetonationLoc), 0);
					
					// shift the detonation location for the debris spawning
					if(bombDetonationLoc.y>4) {
						bombDetonationLoc.y-=1;
					}else bombDetonationLoc.y +=1;
					
					// Spawn the debris
					ArrayList<Debris> debris = new ArrayList<Debris>(6);
					debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, -25, 27));
					debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, -16, 32));
					debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, -6, 35));
					debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, 8, 35));
					debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, 18, 30));
					debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, 30, 23));
					// Wait for them to die, then dispose
					for(Debris d : debris) {
						d.join();
					}
					debris.clear();		// probably unnecessary, but ah well.
					
					// Check game victory
					if(blueTeamLives <= 0) {
							System.out.println("[OK] GAME OVER!");
							System.out.println("[OK] Red Team Victory!");
							break;
					}else if(redTeamLives <= 0){
							System.out.println("[OK] GAME OVER!");
							System.out.println("[OK] Blue Team Victory!");
							break;
					}else {
						// No victory, wait for next key press to signify ready for next round
						nextKeyPress = false;
						while(!nextKeyPress) {
							TimeUnit.MILLISECONDS.sleep(20);
						}
					}
				}	// End infinite loop 1
				
				// Game victory breaks out here
				System.out.println("[OK] Reflex game over...");
				Reflex.device.clearScreen();
				
				System.out.println("[OK] Play again?");
				
				int countdown = 200;
				boolean quit = false;
				for(int i = 1; i<9; i++) {
					Reflex.device.send(Reflex.device.toMidi(9, i), 2);
				}
				nextKeyPress=false;
				while(!nextKeyPress) {
					TimeUnit.MILLISECONDS.sleep(50);
					countdown --;
					Reflex.device.send(Reflex.device.toMidi(9, (int)(countdown*0.05)), 0);
					if(countdown<20) {
						quit = true;
						break;
					}
				}
				if(!quit) {
					this.redTeamLives = 4;
					this.blueTeamLives = 4;
					continue;	
				}else break;
			}
			kill();
		}
	}
	
	private abstract class Missile extends Thread{
		
		public Coordinate loc;
		private Coordinate tailLoc;
		
		private int dir;
		private int color;
		private int vel;
		
		
		public Missile(Coordinate loc) {
			this.loc = loc;
			this.tailLoc = new Coordinate(loc.x, loc.y-1);
			if(this.loc.y < 4) {
				this.dir = 1;
				this.color = 40;
			}else {
				this.dir = -1;
				this.color = 52;
			}
			
			this.vel = Reflex.MISSILE_LAUNCH_VEL;	
			
			this.start(); 	// Run self
		}
		
		
		public void run() {
			int delay = 1000/this.vel;
			this.loc.shiftY(dir);
			try {
				while(!loc.offScreen() && Reflex.gameOn) {
					if(checkCollision(dir)) {
						break;	// break out of loop
					}else {
						// Draw
						Reflex.device.send(Reflex.device.toMidi(loc), this.color);
						Reflex.device.send(Reflex.device.toMidi(tailLoc), this.color + 3);
						
						// Delay
						delay = 1000000/vel;
						try {
							TimeUnit.MILLISECONDS.sleep(delay);
						} catch (InterruptedException e) {
							break;
						}
						// Update velocity
						this.vel += Reflex.MISSILE_ACC*delay*0.001;
						
						// Clear from screen
						Reflex.device.send(Reflex.device.toMidi(loc), 0); // Turn off
						Reflex.device.send(Reflex.device.toMidi(tailLoc), 0); // Turn off
						
						// Calculate next pos
						this.tailLoc = this.loc.copy();
						this.loc.shiftY(dir);
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		
		public abstract boolean checkCollision(int dir) throws InvalidMidiDataException;
	}
	
	private class Debris extends Thread{
		
		
		private double x;
		private double y;
		private double xvel;
		private double yvel;
		private double yacc;
		
		private final int refreshRate = 30;
		
		public Debris(int x, int y, int angle, int vel) {
			this.x = x;
			this.y = y;
			
			this.xvel = vel*Math.sin(Math.toRadians(angle));
			this.yvel = vel*Math.cos(Math.toRadians(angle));
			
			this.yacc = 100;
			
			if(this.y < 4) {
				this.yacc = - this.yacc;
			}else {
				this.yvel = -this.yvel;
			}
			this.start();
		}
		
		public void run() {
			try{
				while(true) {
					//Draw
					Reflex.device.send(Reflex.device.toMidi((int)(x+0.5), (int)(y+0.5)), 3);
					TimeUnit.MILLISECONDS.sleep(refreshRate);
					Reflex.device.send(Reflex.device.toMidi((int)(x+0.5), (int)(y+0.5)), 0);
					// Update yvel
					yvel += yacc*(refreshRate*0.001);
					// Update position
					this.x += xvel*refreshRate*0.001;
					this.y += yvel*refreshRate*0.001;
					
					// Process reflections
					if(this.y < 1.5) {
						this.y = 3-this.y;
						this.yvel = - this.yvel*0.9;
					}else if(this.y>7.5) {
						this.y = 15 - this.y;
						this.yvel = -this.yvel*0.9;
					}
					
					if(this.x < 1) {
						break;
					}else if(this.x>8) {
						break;
					}
					
				}
				
			}catch(InterruptedException e) {
				e.printStackTrace();
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
	}
	
	private abstract class Bomb extends Thread{
		public Coordinate loc;
		
		private STATES state;
		private Random random;
		
		private int colors[] = {4, 5, 6};
		private int colorCounter = 0;
		private int stateCounter = 0;
		private int strike;
		
		private boolean stateChange = true;
		
		
		public Bomb(int x) {
			Reflex.bombCounterMod = 150;
			Reflex.activeBombs = 0;
			strike = 0;
			
			random = new Random();
			this.loc = new Coordinate(x, random.nextInt(2)+4);
			this.state = STATES.INVINCIBLE;
			this.stateCounter = 20 + random.nextInt(10);
			
			this.start();
		}
		
		public void run() {
			try {
				while(Reflex.gameOn) {
					if(stateChange) {
						colorCounter = 0;
						switch(this.state) {
						case ACTIVE:
							colors = new int[] {5, 6, 7};
							break;
							
						case PASSIVE:
							colors = new int[] {21, 22, 23};
							break;
						
						default:
							colors = new int[] {109, 99, 100, 99};
							break;
						}
						
						stateChange = false;
					}
					
					Reflex.device.send(Reflex.device.toMidi(loc), colors[colorCounter]);
					colorCounter++;
					colorCounter %= colors.length;
					
					if(stateCounter>0) {
						if(this.state == Reflex.STATES.ACTIVE) {
							stateCounter -= (3-Reflex.activeBombs);
						}else {
							stateCounter-= 1;
						}
						
					}else {
						switch(state) {
						case ACTIVE:
							setBombState(STATES.PASSIVE);
							break;
							
						default:
							setBombState(STATES.ACTIVE);
							break;
						}
					}
					
					TimeUnit.MILLISECONDS.sleep(70);
					
				}
			}catch(InterruptedException e) {
				
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
			
		}
		
		private void setBombState(STATES state) {
			if(this.state != state) {
				if(this.state == Reflex.STATES.PASSIVE) {
					Reflex.activeBombs--;
				}else if(state == Reflex.STATES.PASSIVE) {
					Reflex.activeBombs++;
				}
				this.state = state;
				this.stateChange = true;	
			}
			
			// COUNTER DEFINITIONS
			switch(this.state) {
			case ACTIVE:
				// Active duration
				this.stateCounter = (int) (10 + random.nextInt(10 + 3*bombCounterMod));
				break;
			case PASSIVE:
				this.stateCounter = 20;
				break;
			default:
				// Passive and Invincibility duration
				this.stateCounter = 10;
				break;
			}
			
		}
		
		/**
		 * nudge the bomb along the y-axis in the direction dictated by
		 * 'dir'. If the bomb touches a barricade, it calls detonate. 
		 * @param dir
		 * @throws InvalidMidiDataException 
		 */
		private void nudge(int dir) throws InvalidMidiDataException {
			Reflex.device.send(Reflex.device.toMidi(loc), 0);
			this.loc.shiftY(dir);
			
			// Speed modifier
			if(bombCounterMod > 2) {
				Reflex.bombCounterMod = (int) (Reflex.bombCounterMod*0.94);	
			}
			
			if((this.loc.y==8)||(this.loc.y==1)) {
				this.detonate(this.loc);
			}else {
				setBombState(STATES.INVINCIBLE);
			}
		}
		
		public void impact(int dir) throws InvalidMidiDataException {
			switch (this.state) {
			case ACTIVE:
				if(((this.loc.y == 7) && (dir == -1)) || ((this.loc.y == 2) && (dir == 1))) {
					strike ++;
					if(strike>2) {
						this.nudge(-dir);
					}
				}else {
					this.nudge(-dir);
				}
				break;
				
			case PASSIVE:
				this.nudge(dir);
				break;
				
			default:
				break;
			}
		}
		
		public abstract void detonate(Coordinate c);
	}
}
