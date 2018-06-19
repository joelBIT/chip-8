package joelbits.emulator;

import java.net.URI;

public class GameSettings {
	private int VELOCITY = 10;
	private boolean running;
	private boolean paused;
	private URI gamePath;
	
	public void setVelocity(int velocity) {
		VELOCITY = velocity;
	}
	
	public int getVelocity() {
		return VELOCITY;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public void setGamePath(URI gamePath) {
		this.gamePath = gamePath;
	}
	
	public URI getGamePath() {
		return gamePath;
	}
}
