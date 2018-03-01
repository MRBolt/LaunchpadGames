package launchpad;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;


public interface Launchpad {
	public boolean isConnected();
	public void connectToDevice(String deviceCue)throws MidiUnavailableException;
	public void send(int midi, int vel) throws InvalidMidiDataException;
	public void setReceiver(Receiver receiver);
	public void clearScreen() throws InvalidMidiDataException;
	public void fill(int x1, int y1, int x2, int y2, int color) throws InvalidMidiDataException;
	public void kill();
	public int toMidi(Coordinate c);
	public int toMidi(int x, int y);
	public Coordinate toCoordinate(int midi);
}
