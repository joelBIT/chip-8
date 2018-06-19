package joelbits.emulator;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterpreterConfig {
	private static final Logger log = LoggerFactory.getLogger(InterpreterConfig.class);
	private final Properties properties = new Properties();

	public InterpreterConfig() {
		try {
			properties.load(this.getClass().getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			log.error(e.toString(), e);
		}
	}
	
	public int canvasWidth() {
		return Integer.parseInt(properties.getProperty("canvasWidth"));
	}
	
	public int canvasHeight() {
		return Integer.parseInt(properties.getProperty("canvasHeight"));
	}
	
	public int screenWidth() {
		return Integer.parseInt(properties.getProperty("screenWidth"));
	}
	
	public int screenHeight() {
		return Integer.parseInt(properties.getProperty("screenHeight"));
	}
	
	public int pixelSize() {
		return Integer.parseInt(properties.getProperty("pixelSize"));
	}
}
