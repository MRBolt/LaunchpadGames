package launchpad;
	
public abstract class MidiMap{
	/**
	 * This just allows for inheritance whilst maintaining static methods
	 * @param x
	 * @param y
	 * @return
	 */
	public abstract byte toMidi_nonStatic(byte x, byte y);
	
	/**
	 * Launchpad Mk II Midi Mapping <p>
	 * Midi layout of structure "YX", starting with minimum of (MIDI)11 at bottom left pad.<p>
	 * These layouts take into account CC buttons (y = 9 row) and start from (CC)104.
	 * <p>(x = 1, y = 1) = 11
	 * <p>(x = 4, y = 8) = 83 
	 * <p>(x = 4, y = 9) = 107
	 * @author Marco
	 *
	 */
	public static class LaunchpadMk2 extends MidiMap{
		
		/**
		 * Convert from x y to midi (or CC if y>=9)
		 * @param x
		 * @param y
		 * @return
		 */
		public static byte toMidi(byte x, byte y) {
			if(y<9) {
			return (byte) (y*10 + x);
			}else {
			return (byte) (y*10 + x + 13);
			}
		}
		
		public byte toMidi_nonStatic(byte x, byte y) {
			return toMidi(x, y);
		}
		
		public static byte[] toXY(byte midi) {
			byte temp[] = {0, 0};
			if(midi > 90) {
			// CC starts at 104, bring down to 91 then convert
			midi -= 13;
			}
			temp[0] = (byte) (midi%10);
			temp[1] = (byte) ((midi - temp[0])*0.1);
			return temp;
		}
	}
	
	public static class LaunchpadMini extends MidiMap{
	
		//@Override
		public byte toMidi(byte x, byte y) {
			if(y<9)
				return (byte) ((x-1) + (8-y)*16);
			else
				return (byte) 0;
		}
	
		//@Override
		public byte[] toXY(byte midi) {
			byte[] temp = {0, 0};
			temp[0] = (byte) (midi%16);
			temp[1] = (byte) ((midi-temp[0])/16);
			temp[0] += 1;
			temp[1] = (byte) (8-temp[1]);
			return temp;
		}

		@Override
		public byte toMidi_nonStatic(byte x, byte y) {
			return toMidi(x, y);
		}
	
	}
}