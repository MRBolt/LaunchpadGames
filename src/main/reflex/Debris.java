package reflex;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import launchpad.Launchpad;

public class Debris extends Thread{
	
	
	private double x;
	private double y;
	private double xvel;
	private double yvel;
	private double yacc;
	private Random random;
	private Launchpad device;
	private boolean running;
	
	private final int refreshRate = 30;
	
	public Debris(int x, int y, int angle, Launchpad device) {
		this.device = device;
		this.x = x;
		this.y = y;
		
		this.xvel = 30*Math.sin(Math.toRadians(angle));
		this.yvel = 30*Math.cos(Math.toRadians(angle));
		
		this.yacc = 100;
		
		if(this.y < 4) {
			this.yacc = - this.yacc;
		}else {
			this.yvel = -this.yvel;
		}
		this.start();
	}
	
	public void run() {
		this.running = true;
		try{
			while(running) {
				//Draw
				device.sendNE(Launchpad.toMidi((int)(x+0.5), (int)(y+0.5)), 3);
				TimeUnit.MILLISECONDS.sleep(refreshRate);
				device.sendNE(Launchpad.toMidi((int)(x+0.5), (int)(y+0.5)), 0);
				// Update yvel
				yvel += yacc*(refreshRate*0.001);
				// Update position
				this.x += xvel*refreshRate*0.001;
				this.y += yvel*refreshRate*0.001;
				
				// Process reflections
				if(this.y < 1.5) {
					this.y = 3-this.y;
					this.yvel = - this.yvel*0.8;
					//this.xvel =this.xvel*0.8;
				}else if(this.y>7.5) {
					this.y = 15 - this.y;
					this.yvel = -this.yvel*0.8;
					//this.xvel = this.xvel*0.8;
				}
				
				if(this.x < 1) {
					//this.x = 2-this.x;
					//this.yvel = this.yvel*0.8;
					//this.xvel = - this.xvel*0.6;
					break;
				}else if(this.x>8) {
					//this.x = 16 - this.x;
					//this.yvel = this.yvel*0.8;
					//this.xvel = -this.xvel*0.6;
					break;
				}
				
			}
			
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Exiting");
		
		
		
	}
	
	public void stopIt() {
		try {
			this.running = false;
			this.join(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
