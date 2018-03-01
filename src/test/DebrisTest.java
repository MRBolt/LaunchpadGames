import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.MidiUnavailableException;

import launchpad.LaunchpadMK2;
import reflex.Debris;
import reflex.Reflex;

public class DebrisTest {
	public static LaunchpadMK2 device;
	public DebrisTest(){
	}
	public static void main(String args[]) {
		Random random = new Random();
		ArrayList<Debris> debris = new ArrayList<Debris>(4);
		try {
			device = new LaunchpadMK2();
		} catch (MidiUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(-1);
		}
		
		while(true) {
			System.out.println("Spawning");
			int x = - 30;
			for(int i = 0; i < 4; i++) {
				debris.add(new Debris(4, 2, x, device));
				x+= 20;
			}
			try {
				TimeUnit.SECONDS.sleep(4);
				for(Debris d : debris) {
					d.stopIt();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
