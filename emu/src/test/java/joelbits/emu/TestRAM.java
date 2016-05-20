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
		target.write(0xA, 88);
		
		assertEquals(0xA, target.read(88));
	}
	
	@Test
	public void returnZeroBecauseMemoryHasBeenCleared() {
		target.write(0xA, 88);
		target.clear();
		
		assertEquals(0, target.read(88));
	}
}
