package joelbits.emu;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import joelbits.emu.memory.Memory;
import joelbits.emu.memory.RAM;

public class TestRAM {
private Memory target;
	
	@Before
	public void setUp() {
		target = new RAM();
	}
	
	@Test
	public void returnZeroBecauseMemoryLocationHasNotBeenWrittenTo() {
		assertEquals(0, target.read(88));
	}
	
	@Test
	public void returnNewValueFromMemoryLocation() {
		target.write(88, 0xA);
		
		assertEquals(0xA, target.read(88));
	}
	
	@Test
	public void returnZeroBecauseMemoryHasBeenCleared() {
		target.write(88, 0xA);
		target.clear();
		
		assertEquals(0, target.read(88));
	}
}