package launchpadGames;

import launchpad.Launchpad;

public interface LaunchpadGame {
	public void setDevice(Launchpad device);
	public void play() throws Exception;
}
