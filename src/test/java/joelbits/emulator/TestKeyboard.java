package joelbits.emulator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import javafx.scene.input.KeyCode;
import joelbits.emulator.input.Keyboard;

public class TestKeyboard {
	private Keyboard target;
	
	@Before
	public void setUp() {
		target = new Keyboard();
	}
	
	@Test
	public void sameKeyShouldBeTheCurrentlyPressedKey() {
		target.press(KeyCode.R);

		assertEquals(8, (int) target.currentlyPressed());
	}
	
	@Test
	public void returnZeroSinceTheCurrentlyPressedKeyIsNotValid() {
		target.press(KeyCode.L);

		assertEquals(0, (int) target.currentlyPressed());
	}
	
	@Test
	public void returnZeroWhenKeyReleased() {
		target.press(KeyCode.R);
		target.releasePressed();

		assertEquals(0, (int) target.currentlyPressed());
	}
}
