package connect4;

import java.util.concurrent.TimeUnit;

import javax.sound.midi.InvalidMidiDataException;

import launchpad.Launchpad;
import launchpad.LaunchpadListener;
import launchpad.Coordinate;

public class Connect4 {
	private int grid[];
	public static Launchpad device;
	
	public Connect4() {
		grid = new int[64];
		// set all to 0
		for(int i=0; i<64; i++) {
			grid[i] = 0;
		}
	}
	
	public void setDevice(Launchpad device) {
		Connect4.device = device;
	}
	
	public void play() {
		Connect4.device.setReceiver(new LaunchpadListener() {

			@Override
			public void action(byte midi, byte vel) {
				if(vel >0) {	// note on
					Coordinate c = Connect4.device.toCoordinate(midi);
				}
			}
			
		});
	}
	
	public void drop(int collumn, int color) throws InvalidMidiDataException, InterruptedException {
		if(grid[Connect4.gridMap(collumn, 8)] == 0) {
			// Flash collumn
			Connect4.device.fill(collumn, 1, collumn, 8, 3);
			TimeUnit.MILLISECONDS.sleep(50);
			this.drawSlots(collumn, 1, collumn, 8);
			
			int y = 8;
			// Begin drop
			while(Connect4.gridMap(collumn, y-1) > 0) {
				Connect4.device.send(device.toMidi(collumn, y), color);
				TimeUnit.MILLISECONDS.sleep(70);
				Connect4.device.send(device.toMidi(collumn, y), 0);
				
				y--;
				// check if at bottom
				if(y==1) {
					break;
				}
			}
			// Impact Flash
			Connect4.device.send(device.toMidi(collumn, y), 3);
			TimeUnit.MILLISECONDS.sleep(70);
			// Update and draw slot
			grid[Connect4.gridMap(collumn, y)] = color;
			this.drawSlot(collumn,  y);
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
}
