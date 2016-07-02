package joelbits.emu;

import static org.junit.Assert.*;

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
		
		assertTrue(testRegister.read().equals(0xA));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void addSmallValues() {
		testRegister.write(0x3);
		target.add(testRegister, 0x8);
		
		assertTrue(testRegister.read().equals(0xB));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void addLargeValues() {
		testRegister.write(145);
		target.add(testRegister, 0x3455);
		
		assertTrue(testRegister.read().equals(0xE6));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void addWithRandom() {
		target.addWithRandom(testRegister, 0x2);
		
		assertTrue(testRegister.read().equals(0x2 & randomNumberGenerator.value()));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void addAndDoNotSetCarry() {
		testRegister.write(0x21);
		target.addWithCarry(testRegister, 0x2, FIT_8BIT_REGISTER);
		
		assertTrue(testRegister.read().equals(0x23));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void addAndSetCarry() {
		testRegister.write(0xB0);
		target.addWithCarry(testRegister, 0x3, FIT_8BIT_REGISTER);
		
		assertTrue(testRegister.read().equals(0xB3));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void subtractAndDoNotSetBorrow() {
		testRegister.write(0x3);
		target.subtractWithNegatedBorrow(testRegister, 0x2);
		
		assertTrue(testRegister.read().equals(0xFF));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}

	@Test
	public void subtractAndSetBorrow() {
		testRegister.write(0x2);
		target.subtractWithNegatedBorrow(testRegister, 0x3);
		
		assertTrue(testRegister.read().equals(0x1));
		assertTrue(dataRegisterVF.read().equals(0x1));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void subtractAndSetNegatedBorrow() {
		testRegister.write(0x3);
		target.subtractWithNegatedBorrow(testRegister, 0x2);
		
		assertTrue(testRegister.read().equals(0xFF));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void subtractAndDoNotSetNegatedBorrow() {
		testRegister.write(0x2);
		target.subtractWithNegatedBorrow(testRegister, 0x5);
		
		assertTrue(testRegister.read().equals(0x3));
		assertTrue(dataRegisterVF.read().equals(0x1));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void bitwiseOR() {
		testRegister.write(0x21);
		target.bitwiseOR(testRegister, 0x94);
		
		assertTrue(testRegister.read().equals(0xB5));
		assertTrue(programCounter.read().equals(0x202));
	}

	@Test
	public void bitwiseAND() {
		testRegister.write(0xAC);
		target.bitwiseAND(testRegister, 0xF2);
		
		assertTrue(testRegister.read().equals(0xA0));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void bitwiseXOR() {
		testRegister.write(0xFC);
		target.bitwiseXOR(testRegister, 0x72);
		
		assertTrue(testRegister.read().equals(0x8E));
		assertTrue(programCounter.read().equals(0x202));
	}

	
	@Test
	public void leftShiftWithCarryNotSet() {
		testRegister.write(0x30);
		target.leftShiftWithCarry(testRegister);
		
		assertTrue(testRegister.read().equals(0x60));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}

	@Test
	public void leftShiftWithCarrySet() {
		testRegister.write(0xCE);
		target.leftShiftWithCarry(testRegister);
		
		assertTrue(testRegister.read().equals(0x9C));
		assertTrue(dataRegisterVF.read().equals(0x1));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void rightShiftWithCarryNotSet() {
		testRegister.write(0x30);
		target.rightShiftWithCarry(testRegister);
		
		assertTrue(testRegister.read().equals(0x18));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void rightShiftWithCarrySet() {
		testRegister.write(0x21);
		target.rightShiftWithCarry(testRegister);
		
		assertTrue(testRegister.read().equals(0x10));
		assertTrue(dataRegisterVF.read().equals(0x1));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void doNotskipNextSinceNotEqual() {
		testRegister.write(0x55);
		target.skipNextIfEqual(testRegister, 0x50);
		
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void skipNextBecauseEqual() {
		testRegister.write(0x55);
		target.skipNextIfEqual(testRegister, 0x55);
		
		assertTrue(programCounter.read().equals(0x204));
	}
	
	@Test
	public void skipNextBecauseNotEqual() {
		testRegister.write(0x55);
		target.skipNextIfNotEqual(testRegister, 0x50);
		
		assertTrue(programCounter.read().equals(0x204));
	}
	
	@Test
	public void doNotSkipNextBecauseEqual() {
		testRegister.write(0x55);
		target.skipNextIfNotEqual(testRegister, 0x55);
		
		assertTrue(programCounter.read().equals(0x202));
	}
}
