package reflex;

import java.util.concurrent.TimeUnit;

import javax.sound.midi.InvalidMidiDataException;

import common.Coordinate;

public abstract class Missile extends Thread{
	
	public Coordinate loc;
	private Coordinate tailLoc;
	
	private int dir;
	private int color;
	private int vel;
	
	
	public Missile(Coordinate loc) {
		this.loc = loc;
		this.tailLoc = new Coordinate(-1, -1);
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
