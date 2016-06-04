package joelbits.emu;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import joelbits.emu.memory.DirtyBuffer;

public class TestDirtyBuffer {
	private DirtyBuffer target;
	
	@Before
	public void setUp() {
		target = new DirtyBuffer();
	}
	
	@Test
	public void dirtyLocationsAreRetrievedInTheSameOrderAsTheyWereWritten() {
		target.write(0x200, 0x55);
		target.write(0x209, 0x57);
		
		assertEquals(0x200, target.read(0x203));
		assertEquals(0x209, target.read(0x244));
	}
	
	@Test
	public void bufferSizeIsZeroWhenCleared()  {
		target.write(0x200, 0x55);
		target.clear();
		
		assertEquals(0, target.size());
	}
}
