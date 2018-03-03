package launchpadGames;

import java.util.concurrent.TimeUnit;

import javax.sound.midi.InvalidMidiDataException;

import connect4.Connect4;
import launchpad.*;
import reflex.Reflex;

public class LaunchpadGames {


	private static boolean waiting = true;
	private static boolean running = true;

	private static LaunchpadGame selectedGame;
	private static Launchpad device;
	
	public static void main(String args[]) {
		
		try {
			device = Launchpad.getLaunchpad();
			Reflex.device = device;
			Connect4.device = device;
			device.clearScreen();
			
			while(running) {
				
				device.setReceiver(new LaunchpadListener() {

					@Override
					public void action(byte midi, byte vel) {
						if(vel>0) {
							
							if(device.toCoordinates(midi)[0] <5) {
								selectedGame = new Reflex();
							}else if(device.toCoordinates(midi)[0] < 9) {
								selectedGame = new Connect4();
							}else {
								switch(device.toCoordinates(midi)[1]) {
								case 4: 
									running = false;
									break;
								default:
									break;
								}
							}	
							
							waiting = false;
						}
					}
					
				});
				// Draw options to screen
				drawReflexDemo();
				drawConnect4Demo();
				waiting = true;
				while(waiting) {
					TimeUnit.MILLISECONDS.sleep(20);
				}
				
				if(running) {
					if(selectedGame!=null) {
						selectedGame.play();
					}
				}else break;
				
			}
			device.clearScreen();
			device.kill();
		}catch(Exception e) {
			System.out.println("[ER] Error has caused system to exit!");
			e.printStackTrace();
		}
		
	}
	
	private static void drawReflexDemo() throws InvalidMidiDataException {
		for(int i = 1; i<4; i++) {
			device.send(device.toMidi(i, 1), 40+i);
			device.send(device.toMidi(i, 8), 52+i);
		}
		device.send(device.toMidi(4, 1), 42);
		device.send(device.toMidi(4, 8), 54);
		
		device.send(device.toMidi(1, 4), 5);
		device.send(device.toMidi(2, 5), 5);
		device.send(device.toMidi(3, 4), 21);
		device.send(device.toMidi(4, 5), 5);
	}
	
	private static void drawConnect4Demo() throws InvalidMidiDataException {
		device.send(device.toMidi(5, 1), 53);
		device.send(device.toMidi(6, 1), 41);
		device.send(device.toMidi(7, 1), 53);
		device.send(device.toMidi(8, 1), 41);
		
		device.send(device.toMidi(5, 2), 53);
		device.send(device.toMidi(6, 2), 53);
		device.send(device.toMidi(7, 2), 41);
		device.send(device.toMidi(8, 2), 53);
		
		device.send(device.toMidi(5, 3), 3);
		device.send(device.toMidi(6, 3), 41);
		device.send(device.toMidi(7, 3), 53);
		device.send(device.toMidi(8, 3), 41);
		
		device.send(device.toMidi(5, 4), 53);
		device.send(device.toMidi(6, 4), 3);
		device.send(device.toMidi(7, 4), 53);
		device.send(device.toMidi(8, 4), 41);
		
		device.send(device.toMidi(5, 5), 53);
		device.send(device.toMidi(6, 5), 0);
		device.send(device.toMidi(7, 5), 3);
		device.send(device.toMidi(8, 5), 53);
		
		device.send(device.toMidi(5, 6), 0);
		device.send(device.toMidi(6, 6), 0);
		device.send(device.toMidi(7, 6), 41);
		device.send(device.toMidi(8, 6), 3);
	}
}
