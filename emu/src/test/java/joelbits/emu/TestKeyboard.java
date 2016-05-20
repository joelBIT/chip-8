package joelbits.emu;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import javafx.scene.input.KeyCode;
import joelbits.emu.input.Keyboard;

public class TestKeyboard {
	private Keyboard target;
	
	@Before
	public void setUp() {
		target = new Keyboard();
	}
	
	@Test
	public void sameKeyShouldBeTheCurrentlyPressedKey() {
		target.pressKey(KeyCode.R);
		
		assertEquals(8, target.getCurrentlyPressedKey());
	}
	
	@Test
	public void returnZeroSinceTheCurrentlyPressedKeyIsNotValid() {
		target.pressKey(KeyCode.L);
		
		assertEquals(0, target.getCurrentlyPressedKey());
	}
	
	@Test
	public void returnZeroWhenKeyReleased() {
		target.pressKey(KeyCode.R);
		target.releasePressedKey();
		
		assertEquals(0, target.getCurrentlyPressedKey());
	}
}
