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
		target.press(KeyCode.R);
		
		assertEquals(8, target.currentlyPressed().intValue());
	}
	
	@Test
	public void returnZeroSinceTheCurrentlyPressedKeyIsNotValid() {
		target.press(KeyCode.L);
		
		assertEquals(0, target.currentlyPressed().intValue());
	}
	
	@Test
	public void returnZeroWhenKeyReleased() {
		target.press(KeyCode.R);
		target.releasePressed();
		
		assertEquals(0, target.currentlyPressed().intValue());
	}
}
