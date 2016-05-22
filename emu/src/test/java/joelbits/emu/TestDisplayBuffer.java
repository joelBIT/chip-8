package joelbits.emu;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import joelbits.emu.output.DisplayBuffer;

public class TestDisplayBuffer {
	private DisplayBuffer target;
	private int SCREEN_WIDTH = 64;
	private int SCREEN_HEIGHT = 32;

	@Before
	public void setUp() {
		target = new DisplayBuffer(SCREEN_WIDTH, SCREEN_HEIGHT);
	}
	
	@Test
	public void sameSizeAsWidthTimesHeight() {
		assertEquals(SCREEN_WIDTH*SCREEN_HEIGHT, target.size());
	}
	
	@Test
	public void writtenDataReturnedWhenLocationRead() {
		target.write(0x200, 0x55);
		
		assertEquals(0x55, target.read(0x200));
	}
	
	@Test
	public void allBufferLocationsAreZeroAfterBufferCleared() {
		target.write(0x200, 0x55);
		target.clear();
		
		for (int i = 0; i < SCREEN_WIDTH*SCREEN_HEIGHT; i++) {
			assertEquals(0, target.read(i));
		}
	}
}
