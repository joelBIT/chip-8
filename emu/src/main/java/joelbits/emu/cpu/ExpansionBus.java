package joelbits.emu.cpu;

import joelbits.emu.input.Keyboard;
import joelbits.emu.output.Display;
import joelbits.emu.output.Sound;

/**
 * Used by the CPU for I/O operations.
 * 
 * @author rollnyj
 *
 */
public class ExpansionBus {
	private final Display display = new Display();
	private final Keyboard keyboard = new Keyboard();
	private final Sound sound = new Sound();
	
	public Display getDisplay() {
		return display;
	}
	
	public Keyboard getKeyboard() {
		return keyboard;
	}
	
	public Sound getSound() {
		return sound;
	}
}
