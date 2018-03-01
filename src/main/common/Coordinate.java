package common;

public class Coordinate {
	public int x;
	public int y;
	
	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void shift(int x, int y) {
		this.x += x;
		this.y += y;
	}
	
	public void shiftY(int y) {
		this.y += y;
	}
	
	public boolean offScreen() {
		if((this.y > 9) || (this.x > 9) || (this.y < 1) || (this.x < 1)){
			return true;
		}else return false;
	}
	
	public Coordinate copy() {
		return new Coordinate(x, y);
	}
}
