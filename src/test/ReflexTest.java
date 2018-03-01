import launchpad.LaunchpadMK2;
import reflex.*;

public class ReflexTest {
	public static void main(String args[]) {
		
		try {
			Reflex r = new Reflex();
			r.setDevice(new LaunchpadMK2());
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
