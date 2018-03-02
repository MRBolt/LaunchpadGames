package launchpad;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

/**
 * A class that allows simple communication with a launchpad MIDI device
 * @author Marco
 *
 */
public class LaunchpadMini implements Launchpad {
	
	private MidiDevice deviceIn;
	private MidiDevice deviceOut;
	private Transmitter lpTransmitter;
	private Receiver lpReceiver;
	
	/**
	 * Establish connection with a Launchpad MK2 device. 
	 * @throws MidiUnavailableException
	 */
	public LaunchpadMini() throws MidiUnavailableException{
		
		deviceIn = null;
		deviceOut = null;
		
		// Stage 1: Establish connection
		// Get information about all devices
		
		// Look for deviceCue in any of the infos
		for(MidiDevice.Info i : MidiSystem.getMidiDeviceInfo()) {
			if(i.getName().contains("Launchpad Mini")) {
				// Check to see if its a receiver or transmitter
				if(MidiSystem.getMidiDevice(i).getMaxReceivers()!=0) {
					// Device is output
					this.deviceOut = MidiSystem.getMidiDevice(i);
					this.lpReceiver = this.deviceOut.getReceiver();
				}else if(MidiSystem.getMidiDevice(i).getMaxTransmitters()!=0) {
					// Device is input device
					this.deviceIn = MidiSystem.getMidiDevice(i);
					this.lpTransmitter = MidiSystem.getMidiDevice(i).getTransmitter();
				}
			}
		}
		// Check for errors
		if(this.deviceIn == null) {
			throw new MidiUnavailableException("Input device not found!");
		}else if(this.deviceOut == null) {
			throw new MidiUnavailableException("Output device not found!");
		}else {
			// No errors, open devices
			this.deviceIn.open();
			this.deviceOut.open();
		}
	}
	
	/**
	 * Send a message to the connected device
	 * @param midi
	 * @param vel
	 * @throws InvalidMidiDataException
	 */
	public void send(int midi, int vel) throws InvalidMidiDataException {
		if(midi<104) {
			lpReceiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, midi, vel ), -1);
		}else {
			lpReceiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, midi, vel), -1);
		}
	}
	
	/**
	 * Set a receiver to listens to the launchpad transmitter
	 */
	public void setReceiver(Receiver receiver) {
		this.lpTransmitter.setReceiver(receiver);
	}
	
	public int toMidi(int x, int y) {
		if(y<9) {
			return (byte) ((8-y)*16 + x);
		}else {
			//TODO: figure out what's going on with ctl messages
			return (byte) (-1);
		}
	}
	
	public int toMidi(int[] xy){
		if(xy.length<2) {
			return -1;
		}
		
		if(xy[1]<9) {
			return (byte) (xy[1]*10 + xy[0]);
		}else {
			return (byte) (xy[1]*10 + xy[0] + 13);
		}
	}
	
	public int[] toCoordinates(int midi) {
		int[] coordinates = new int[2];
		
		// Convert top-row buttons to expected value
		if(midi > 103) {
			midi -= 13;
		}
		
		coordinates[1] = 8-midi%16;
		coordinates[0] = midi%16;
		
		return coordinates;
	}
	
	public void clearScreen() throws InvalidMidiDataException {
		for(int x = 1; x < 10; x++) {
			for(int y=1; y<10; y++) {
				this.send(this.toMidi(x, y), 0);
			}
		}
	}
	
	public void fill(int x1, int y1, int x2, int y2, int color) throws InvalidMidiDataException {
		for(int x = x1; x <= x2; x++) {
			for(int y=y1; y<=y2; y++) {
				this.send(this.toMidi(x, y), color);
			}
		}
	}
	
	public void kill() {
		this.lpReceiver.close();
		this.lpTransmitter.close();
		this.deviceIn.close();
		this.deviceOut.close();
	}
	
	
}


