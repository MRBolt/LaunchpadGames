

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import launchpad.Coordinate;
import launchpad.Launchpad;
import launchpad.LaunchpadMK2;
import launchpad.LaunchpadListener;

public class ColorTest {
	Launchpad device;
	
	public ColorTest() throws MidiUnavailableException {
		device = new LaunchpadMK2();
		
		device.setReceiver(new LaunchpadListener() {
			int offset = 0;
			@Override
			public void action(byte midi, byte vel) {
				if(vel>0) {
					switch(midi) {
					case 104:
						for(int j=1; j<9; j++) {
							for(int i=1; i<9; i++) {
								try {
									device.send(device.toMidi(i, j), i + 8*j -9);
								} catch (InvalidMidiDataException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						
						offset = 0;
						break;
					
					case 105:
						for(int j=1; j<9; j++) {
							for(int i=1; i<9; i++) {
								try {
									device.send(device.toMidi(i, j), 64 + i + 8*j -9);
								} catch (InvalidMidiDataException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						offset = 64;
						break;
						
					default:
						Coordinate c = device.toCoordinate(midi);
						System.out.println(offset+c.x-1 +8*c.y-8);
						break;
					}
				}
			}
		});
	}
	
	
	public static void main(String args[]) {
		try {
			ColorTest test = new ColorTest();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
