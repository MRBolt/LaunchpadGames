package reflex;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.InvalidMidiDataException;

import common.Coordinate;
import launchpad.LaunchpadMK2;

public abstract class Bomb extends Thread{
	public static enum STATES {ACTIVE, INVINCIBLE, PASSIVE};
	public static int counterMod;
	public static int activeBombs;
	public Coordinate loc;
	
	private STATES state;
	private Random random;
	
	private int colors[] = {4, 5, 6};
	private int colorCounter = 0;
	private int stateCounter = 0;
	private int strike;
	
	private boolean stateChange = true;
	
	
	public Bomb(int x) {
		Bomb.counterMod = 150;
		Bomb.activeBombs = 0;
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
					if(this.state == Bomb.STATES.ACTIVE) {
						stateCounter -= (3-Bomb.activeBombs);
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
	
	public STATES getBombState() {
		return this.state;
	}
	
	private void setBombState(STATES state) {
		if(this.state != state) {
			if(this.state == Bomb.STATES.PASSIVE) {
				Bomb.activeBombs--;
			}else if(state == Bomb.STATES.PASSIVE) {
				Bomb.activeBombs++;
			}
			this.state = state;
			this.stateChange = true;	
		}
		
		// COUNTER DEFINITIONS
		switch(this.state) {
		case ACTIVE:
			// Active duration
			this.stateCounter = (int) (10 + random.nextInt(10 + 3*counterMod));
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
		if(counterMod > 2) {
			Bomb.counterMod = (int) (Bomb.counterMod*0.94);	
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
