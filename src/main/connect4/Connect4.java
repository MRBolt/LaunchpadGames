package connect4;

import java.util.ArrayList;

import launchpad.Launchpad;

public class Connect4 {
	private ArrayList<PlaceHolder> grid;
	public static Launchpad device;
	
	
	public void drop(int Collumn) {
		
	}
	
	public void redrawZone(int x1, int y1, int x2, int y2) {
		for(int j = y1; j<=y2; j++) {
			for(int i=x1; i<=x2; i++) {
				
			}
		}
	}
	
	
	private static int gridMap(int x, int y) {
		return y*8 + x - 9;
	}
}
