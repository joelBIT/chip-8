package joelbits.emulator.settings;

import lombok.Data;

import java.net.URI;

@Data
public class GameSettings {
	private int velocity = 10;
	private boolean running;
	private boolean paused;
	private URI gamePath;
}
