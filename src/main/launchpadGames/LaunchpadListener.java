package launchpadGames;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

/**
 * A simplified midi receiver
 * @author Marco
 *
 */
public abstract class LaunchpadListener implements Receiver{
	@Override
	public void send(MidiMessage message, long timeStamp) {
		action(message.getMessage()[1], message.getMessage()[2]);
	}
	@Override
	public void close() {
		// Gonna leave empty for now, generally not needed
	}
	
	public abstract void action(byte midi, byte vel);
}
