package joelbits.emu.cpu;

import joelbits.emu.input.Keyboard;
import joelbits.emu.output.Beep;
import joelbits.emu.output.Screen;
import joelbits.emu.output.Sound;

public class ExpansionBus {
	private Keyboard keyboard = new Keyboard();
	private Sound sound = new Beep();
	private Screen<Integer> screen = new Screen<Integer>(64, 32, 14);
	
	public Keyboard getKeyboard() {
		return keyboard;
	}
	
	public Sound getSound() {
		return sound;
	}
	
	public Screen<Integer> getScreen() {
		return screen;
	}
}
