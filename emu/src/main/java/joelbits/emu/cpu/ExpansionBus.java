package joelbits.emu.cpu;

import joelbits.emu.input.Keyboard;
import joelbits.emu.output.Screen;
import joelbits.emu.output.Sound;

public class ExpansionBus<T> {
	private final Keyboard keyboard;
	private final Sound sound;
	private final Screen<T> screen;
	
	public ExpansionBus(Keyboard keyboard, Sound sound, Screen<T> screen) {
		this.keyboard = keyboard;
		this.sound = sound;
		this.screen = screen;
	}
	
	public Keyboard getKeyboard() {
		return keyboard;
	}
	
	public Sound getSound() {
		return sound;
	}
	
	public Screen<T> getScreen() {
		return screen;
	}
}
