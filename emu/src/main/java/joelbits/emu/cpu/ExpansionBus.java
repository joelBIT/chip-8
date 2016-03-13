package joelbits.emu.cpu;

import joelbits.emu.Display;
import joelbits.emu.Keyboard;

/**
 * Used by the CPU for I/O operations.
 * 
 * @author rollnyj
 *
 */
public class ExpansionBus {
	private final Display display = new Display();
	private final Keyboard keyboard = new Keyboard();
	
	public Display getDisplay() {
		return display;
	}
	
	public Keyboard getKeyboard() {
		return keyboard;
	}
}
