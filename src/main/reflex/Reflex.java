package reflex;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
	
	public Reflex() throws Exception{
		if(Reflex.gameOn) {
			throw new Exception("Reflex already running!");
		}
		
		// Startup launchpad
		
		
		System.out.print("[DONE]\nSetting up variables... ");
		bombs = new ArrayList<Bomb>(8);
		
		
		System.out.print("[DONE]\nSetting up new game... ");
		
		Reflex.device.clearScreen();
		
		while(true) {
			while(true) {
				// New Game Setup
				Reflex.gameOn = true;
				
				Reflex.device.clearScreen();
				
				// Draw Lives
				for(int i = 0; i < redTeamLives; i++) {
					Reflex.device.send(Launchpad.toMidi(9, 8-i), redTeamColors[0]);
				}
				for(int i = 0; i < blueTeamLives; i++) {
					Reflex.device.send(Launchpad.toMidi(9, 1+i), blueTeamColors[0]);
				}
				
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
				
				missileEn = true;
				System.out.print("[DONE]\n[OK] Game start!\n");
				
				
				
				while (Reflex.gameOn) {
					try {
						for(int i=1; i<9; i++) {
							Reflex.device.sendNE(Launchpad.toMidi(i, 1), blueTeamColors[colorCounter]);
							Reflex.device.sendNE(Launchpad.toMidi(i, 8), redTeamColors[colorCounter]);
							colorCounter++;
							colorCounter %= 4;
						}
						colorCounter++;
						colorCounter %= 4;
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
						Reflex.gameOn = false;
					}
				}
				
				
				// Explosion graphics

				// Draw flash from detonation line
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
				Reflex.device.sendNE(Launchpad.toMidi(9, 1), 0);
				Reflex.device.sendNE(Launchpad.toMidi(9, 8), 0);
				
				// Update score board
				for(int i = 0; i < redTeamLives; i++) {
					Reflex.device.send(Launchpad.toMidi(9, 8-i), redTeamColors[0]);
				}
				for(int i = 0; i < blueTeamLives; i++) {
					Reflex.device.send(Launchpad.toMidi(9, 1+i), blueTeamColors[0]);
				}
				
				
				System.out.println("[OK] Round over! Score: R=" + redTeamLives + " | B=" + blueTeamLives);
				Reflex.gameOn = false;
				missileEn = false;		// Stop missile launches
				
				// Wait for bombs to die, then dispose
				for(Bomb b : bombs) {
					b.join();
					Reflex.device.sendNE(Launchpad.toMidi(b.loc), 0);
				}
				bombs.clear();
				
				// Display the hole in the wall
				Reflex.device.sendNE(Launchpad.toMidi(bombDetonationLoc), 0);
				
				// shift the detonation location for the debris
				if(bombDetonationLoc.y>4) {
					bombDetonationLoc.y-=1;
				}else bombDetonationLoc.y +=1;
				
				// Spawn the debris
				ArrayList<Debris> debris = new ArrayList<Debris>(6);
	
				debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, -30, device));
				debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, -17, device));
				debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, -6, device));
				debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, 6, device));
				debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, 17, device));
				debris.add(new Debris(bombDetonationLoc.x, bombDetonationLoc.y, 30, device));
				
				// Wait for them to die, then dispose
				for(Debris d : debris) {
					d.join();
				}
				debris.clear();
				
				
				// Check game victory
				if((blueTeamLives <= 0) || (redTeamLives <= 0)) {
					if((blueTeamLives <= 0)) {
						System.out.println("[OK] GAME OVER!");
						System.out.println("[OK] Red Team Victory!");
					}else {
						System.out.println("[OK] GAME OVER!");
						System.out.println("[OK] Blue Team Victory!");
					}
					
					try {
						TimeUnit.MILLISECONDS.sleep(200);
					} catch (InterruptedException e) {}
					
					break;
				}
				
				// if no victory, wait for key press
				nextKeyPress = false;
				while(!nextKeyPress) {
					TimeUnit.MILLISECONDS.sleep(20);
				}
				
				System.out.print("[OK] Setting up new round... ");
			}
			// Game victory breaks out here
			System.out.println("[OK] Reflex game over...");
			Reflex.device.clearScreen();
			
			System.out.println("[OK] Play again?");
			
			int countdown = 200;
			boolean quit = false;
			for(int i = 1; i<9; i++) {
				Reflex.device.sendNE(Launchpad.toMidi(9, i), 2);
			}
			nextKeyPress=false;
			while(!nextKeyPress) {
				TimeUnit.MILLISECONDS.sleep(50);
				countdown --;
				Reflex.device.sendNE(Launchpad.toMidi(9, (int)(countdown*0.05)), 0);
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
	
	public void kill() {
		Reflex.gameOn = false;
		this.missileEn = false;
		Reflex.device.kill();
	}
	
	public void setDevice(Launchpad device) {
		System.out.print("[OK] Setting device... ");
		Reflex.device = device;
		System.out.print("[DONE]\n[OK] Establishing listener... ");
		Reflex.device.setReceiver(new LaunchpadListener() {
			@Override
			public void action(byte midi, byte vel) {
				if(vel != 0){ 		// When button pushed
					// MISSILE LAUNCHES
					if(missileEn){	
						if((Launchpad.toCoordinate(midi).y == 8) || (Launchpad.toCoordinate(midi).y == 1)) {
							new Missile(Launchpad.toCoordinate(midi)) {
								Bomb b = bombs.get(this.loc.x-1);
								@Override
								public boolean checkCollision(int dir) {
									
									if(this.loc.y == b.loc.y) {
										// Impact!
										switch(b.getBombState()) {
										case ACTIVE:
											b.nudge(-dir, 1);
											break;
										
										case PASSIVE:
											b.nudge(dir, 0);
											break;
											
										default:
											break;
										}
										
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
	}
	
}
