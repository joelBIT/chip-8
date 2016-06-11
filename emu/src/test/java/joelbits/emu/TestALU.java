package joelbits.emu;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import joelbits.emu.cpu.ALU;
import joelbits.emu.cpu.registers.DataRegister;
import joelbits.emu.cpu.registers.ProgramCounter;
import joelbits.emu.cpu.registers.Register;

public class TestALU {
	private ALU target;
	private Register<Integer> programCounter;
	private Register<Integer> dataRegisterVF;
	private Register<Integer> testRegister;
	private RandomNumberGenerator randomNumberGenerator;
	private final int FIT_8BIT_REGISTER = 0xFF;
	
	@Before
	public void setUp() {
		programCounter = ProgramCounter.getInstance();
		dataRegisterVF = new DataRegister<Integer>();
		testRegister = new DataRegister<Integer>();
		randomNumberGenerator = new RandomNumberGenerator();
		programCounter.write(0x200);
		
		target = new ALU(programCounter, dataRegisterVF, randomNumberGenerator);
	}

	@Test
	public void loadValueIntoRegister() {
		testRegister.write(0x45);
		target.load(testRegister, 0xA);
		
		assertEquals(0xA, testRegister.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void addSmallValues() {
		testRegister.write(0x3);
		target.add(testRegister, 0x8);
		
		assertEquals(0xB, testRegister.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void addLargeValues() {
		testRegister.write(145);
		target.add(testRegister, 0x3455);
		
		assertEquals(0xE6, testRegister.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void addWithRandom() {
		target.addWithRandom(testRegister, 0x2);
		
		assertEquals(0x2 & randomNumberGenerator.value(), testRegister.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void addAndDoNotSetCarry() {
		testRegister.write(0x21);
		target.addWithCarry(testRegister, 0x2, FIT_8BIT_REGISTER);
		
		assertEquals(0x23, testRegister.read().intValue());
		assertEquals(0x0, dataRegisterVF.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void addAndSetCarry() {
		testRegister.write(0xB0);
		target.addWithCarry(testRegister, 0x3, FIT_8BIT_REGISTER);
		
		assertEquals(0xB3, testRegister.read().intValue());
		assertEquals(0x0, dataRegisterVF.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void subtractAndDoNotSetBorrow() {
		testRegister.write(0x3);
		target.subtractWithNegatedBorrow(testRegister, 0x2);
		
		assertEquals(0xFF, testRegister.read().intValue());
		assertEquals(0x0, dataRegisterVF.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}

	@Test
	public void subtractAndSetBorrow() {
		testRegister.write(0x2);
		target.subtractWithNegatedBorrow(testRegister, 0x3);
		
		assertEquals(0x1, testRegister.read().intValue());
		assertEquals(0x1, dataRegisterVF.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void subtractAndSetNegatedBorrow() {
		testRegister.write(0x3);
		target.subtractWithNegatedBorrow(testRegister, 0x2);
		
		assertEquals(0xFF, testRegister.read().intValue());
		assertEquals(0x0, dataRegisterVF.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void subtractAndDoNotSetNegatedBorrow() {
		testRegister.write(0x2);
		target.subtractWithNegatedBorrow(testRegister, 0x5);
		
		assertEquals(0x3, testRegister.read().intValue());
		assertEquals(0x1, dataRegisterVF.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void bitwiseOR() {
		testRegister.write(0x21);
		target.bitwiseOR(testRegister, 0x94);
		
		assertEquals(0xB5, testRegister.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}

	@Test
	public void bitwiseAND() {
		testRegister.write(0xAC);
		target.bitwiseAND(testRegister, 0xF2);
		
		assertEquals(0xA0, testRegister.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void bitwiseXOR() {
		testRegister.write(0xFC);
		target.bitwiseXOR(testRegister, 0x72);
		
		assertEquals(0x8E, testRegister.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}

	
	@Test
	public void leftShiftWithCarryNotSet() {
		testRegister.write(0x30);
		target.leftShiftWithCarry(testRegister);
		
		assertEquals(0x60, testRegister.read().intValue());
		assertEquals(0x0, dataRegisterVF.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}

	@Test
	public void leftShiftWithCarrySet() {
		testRegister.write(0xCE);
		target.leftShiftWithCarry(testRegister);
		
		assertEquals(0x9C, testRegister.read().intValue());
		assertEquals(0x1, dataRegisterVF.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void rightShiftWithCarryNotSet() {
		testRegister.write(0x30);
		target.rightShiftWithCarry(testRegister);
		
		assertEquals(0x18, testRegister.read().intValue());
		assertEquals(0x0, dataRegisterVF.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void rightShiftWithCarrySet() {
		testRegister.write(0x21);
		target.rightShiftWithCarry(testRegister);
		
		assertEquals(0x10, testRegister.read().intValue());
		assertEquals(0x1, dataRegisterVF.read().intValue());
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void doNotskipNextSinceNotEqual() {
		testRegister.write(0x55);
		target.skipNextIfEqual(testRegister, 0x50);
		
		assertEquals(0x202, programCounter.read().intValue());
	}
	
	@Test
	public void skipNextBecauseEqual() {
		testRegister.write(0x55);
		target.skipNextIfEqual(testRegister, 0x55);
		
		assertEquals(0x204, programCounter.read().intValue());
	}
	
	@Test
	public void skipNextBecauseNotEqual() {
		testRegister.write(0x55);
		target.skipNextIfNotEqual(testRegister, 0x50);
		
		assertEquals(0x204, programCounter.read().intValue());
	}
	
	@Test
	public void doNotSkipNextBecauseEqual() {
		testRegister.write(0x55);
		target.skipNextIfNotEqual(testRegister, 0x55);
		
		assertEquals(0x202, programCounter.read().intValue());
	}
}
