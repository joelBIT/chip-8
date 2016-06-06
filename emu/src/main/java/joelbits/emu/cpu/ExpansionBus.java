package joelbits.emu.cpu;

import joelbits.emu.input.Keyboard;
import joelbits.emu.output.Sound;

public class ExpansionBus<T> {
	private final Keyboard keyboard;
	private final Sound sound;
	
	public ExpansionBus(Keyboard keyboard, Sound sound) {
		this.keyboard = keyboard;
		this.sound = sound;
	}
	
	public Keyboard getKeyboard() {
		return keyboard;
	}
	
	public Sound getSound() {
		return sound;
	}
}
