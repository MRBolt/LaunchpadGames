package reflex;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.InvalidMidiDataException;

import common.Coordinate;
import launchpad.LaunchpadMK2;
import launchpad.Launchpad;
import launchpad.LaunchpadListener;

public class Reflex {
	public static final int MISSILE_LAUNCH_VEL = 30000;
	public static final int MISSILE_ACC = 400000;
	public static boolean gameOn = false;
	public static float speed = 1;
	
	public static Launchpad device;
	
	// IN-GAME STUFF
	private ArrayList<Bomb> bombs;
	private int redTeamLives = 3;
	private int blueTeamLives = 3;
	
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
					this.redTeamLives = 3;
					this.blueTeamLives = 3;
					continue;	
				}else break;
			}
			kill();
		}
	}
	
}
