package reflex;

import java.util.concurrent.TimeUnit;

import javax.sound.midi.InvalidMidiDataException;

import launchpad.LaunchpadMK2;

public class Debris extends Thread{
	
	
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
