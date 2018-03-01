package launchpad;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import common.Coordinate;

/**
 * A class that allows simple communication with a launchpad MIDI device
 * @author Marco
 *
 */
public class LaunchpadMK2 implements Launchpad {
	
	private MidiDevice deviceIn;
	private MidiDevice deviceOut;
	private Transmitter lpTransmitter;
	private Receiver lpReceiver;
	
	/**
	 * Create object and connect to Midi Device with the desired cue
	 * @param deviceCue
	 * @throws MidiUnavailableException
	 */
	public LaunchpadMK2(String deviceCue) throws MidiUnavailableException {
		connectToDevice(deviceCue);
	}
	
	/**
	 * Create object and try connecting to any "launchpad"
	 */
	public LaunchpadMK2() throws MidiUnavailableException{
		connectToDevice("Launchpad");
	}
	
	/**
	 * Check to see if both the output and input are open
	 * @return
	 */
	public boolean isConnected() {
		if(deviceIn.isOpen()) {
			if(deviceOut.isOpen()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Search all connected midi devices for one whos name 
	 * contains the given string.
	 * @param deviceCue
	 * @throws MidiUnavailableException
	 */
	public void connectToDevice(String deviceCue) throws MidiUnavailableException{
		if(this.deviceIn != null) {
			if(this.deviceIn.isOpen()) {
				this.deviceIn.close();
			}
		}
		if(this.deviceOut != null) {
			if(this.deviceOut.isOpen()) {
				this.deviceOut.close();
			}
		}
		
		// Get information about all devices
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		
		// Look for deviceCue in any of the infos
		for(MidiDevice.Info i : infos) {
			if(i.getName().contains(deviceCue)) {
				if(MidiSystem.getMidiDevice(i).getMaxReceivers()<0) {
					// Device is output
					this.deviceOut = MidiSystem.getMidiDevice(i);
					this.lpReceiver = this.deviceOut.getReceiver();
				}else if(MidiSystem.getMidiDevice(i).getMaxTransmitters()<0) {
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
		lpReceiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, midi, vel ), -1);
	}
	
	/**
	 * Send a message to the connected device, ignoring exceptions
	 * @param midi midi note
	 * @param vel velocity (color)
	 */
	public void sendNE(int midi, int vel) {
		try {
			send(midi, vel);
		}catch(InvalidMidiDataException e) {};
	}
	
	/**
	 * Set the receiver that listens to the launchpad transmitter <p>
	 * Reccomended use with {@link LaunchpadListener}
	 */
	public void setReceiver(Receiver receiver) {
		this.lpTransmitter.setReceiver(receiver);
	}
	
	/**
	 * Get a listener <p>
	 * Use to send instructions from other classes or methods <p>
	 * To be honest, you may aswell pass the {@link LaunchpadMK2} object to the class/method and
	 * call {@link LaunchpadMK2.send} from there...
	 * @return 
	 */
	public LaunchpadListener getListener() {
		return new LaunchpadListener() {
			@Override
			public void action(byte midi, byte vel) {
				sendNE(midi, vel);
			}
		};
	}
	
	public int toMidi(int x, int y) {
		if(y<9) {
			return (byte) (y*10 + x);
		}else {
			return (byte) (y*10 + x + 13);
		}
	}
	
	public int toMidi(Coordinate c) {
		if(c.y<9) {
			return (byte) (c.y*10 + c.x);
		}else {
			return (byte) (c.y*10 + c.x + 13);
		}
	}
	
	public Coordinate toCoordinate(int midi) {
		int x;
		int y;
		
		if(midi > 100) {
			midi -= 13;
		}
		
		y = (int)(midi*0.1);
		x = midi-y*10;
		
		return new Coordinate(x, y);
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


