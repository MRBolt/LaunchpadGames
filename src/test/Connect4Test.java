import connect4.Connect4;
import launchpad.LaunchpadMK2;

public class Connect4Test {
	public static void main(String args[]) {
		try {
			Connect4 game = new Connect4();
			game.setDevice(new LaunchpadMK2());
			game.play();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
