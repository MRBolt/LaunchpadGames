package launchpad;

import javax.sound.midi.MidiUnavailableException;

import reflex.Coordinate;

public class ColorTest {
	Launchpad device;
	
	public ColorTest() throws MidiUnavailableException {
		device = new Launchpad();
		
		device.setReceiver(new LaunchpadListener() {
			int offset = 0;
			@Override
			public void action(byte midi, byte vel) {
				if(vel>0) {
					switch(midi) {
					case 104:
						for(int j=1; j<9; j++) {
							for(int i=1; i<9; i++) {
								device.sendNE(Launchpad.toMidi(i, j), i + 8*j -9);
							}
						}
						
						offset = 0;
						break;
					
					case 105:
						for(int j=1; j<9; j++) {
							for(int i=1; i<9; i++) {
								device.sendNE(Launchpad.toMidi(i, j), 64 + i + 8*j -9);
							}
						}
						offset = 64;
						break;
						
					default:
						Coordinate c = Launchpad.toCoordinate(midi);
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
