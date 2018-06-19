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
		
		assertTrue(target.currentlyPressed().equals(8));
	}
	
	@Test
	public void returnZeroSinceTheCurrentlyPressedKeyIsNotValid() {
		target.press(KeyCode.L);
		
		assertTrue(target.currentlyPressed().equals(0));
	}
	
	@Test
	public void returnZeroWhenKeyReleased() {
		target.press(KeyCode.R);
		target.releasePressed();
		
		assertTrue(target.currentlyPressed().equals(0));
	}
}
