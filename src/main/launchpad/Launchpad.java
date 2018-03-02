package launchpad;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;


public interface Launchpad {
	/**
	 * Send a midi message to the connected device
	 * @param midi
	 * @param vel
	 * @throws InvalidMidiDataException
	 */
	public void send(int midi, int vel) throws InvalidMidiDataException;
	
	/**
	 * Set the receiver that the launchpad forwards midi messages to
	 * @param receiver receiver to be linked to the device transmitter
	 */
	public void setReceiver(Receiver receiver);
	
	/**
	 * Turn all LEDs on the device off
	 * @throws InvalidMidiDataException
	 */
	public void clearScreen() throws InvalidMidiDataException;
	
	/**
	 * Fill area with a specified color
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param color
	 * @throws InvalidMidiDataException
	 */
	public void fill(int x1, int y1, int x2, int y2, int color) throws InvalidMidiDataException;
	
	/**
	 * Close connection with device
	 */
	public void kill();
	
	/**
	 * Convert x and y coordinates to the corresponding midi value
	 * @param x
	 * @param y
	 * @return
	 */
	public int toMidi(int x, int y);
	
	/**
	 * Convert midi value to x and y coordinates, stored in an array.<p>
	 * @param midi
	 * @return int[x, y]
	 */
	public int[] toCoordinates(int midi);
	
	/**
	 * Get the first available supported launchpad
	 * @return
	 * @throws MidiUnavailableException
	 */
	public static Launchpad getLaunchpad() throws MidiUnavailableException {
		for(MidiDevice.Info i : MidiSystem.getMidiDeviceInfo()) {
			if(i.getName().contains("Launchpad MK2")) {
				// A MK2 launchpad is connected. setup and return with a MK2
				return new LaunchpadMK2();
			}
		}
		return new LaunchpadMK2();
	}
	
	/**
	 * For debugging purposes. prints a list of all devices to console
	 */
	public static void printDevices() {
		MidiDevice.Info infos[] = MidiSystem.getMidiDeviceInfo();
		
		for(Info i : infos) {
			System.out.println(i.toString());
		}
	}
}
