import launchpad.Launchpad;
import reflex.*;

public class ReflexTest {
	public static void main(String args[]) {
		
		try {
			Reflex r = new Reflex();
			r.setDevice(new Launchpad());
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
