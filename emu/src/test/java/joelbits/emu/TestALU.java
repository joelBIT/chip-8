package joelbits.emu;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import joelbits.emu.cpu.ALU;
import joelbits.emu.cpu.registers.DataRegister;
import joelbits.emu.cpu.registers.ProgramCounter;
import joelbits.emu.cpu.registers.Register;
import joelbits.emu.utils.RandomNumberGenerator;

public class TestALU {
	private ALU target;
	private Register<Integer> programCounter;
	private Register<Integer> dataRegisterVF;
	private Register<Integer> register;
	private RandomNumberGenerator randomNumberGenerator;
	private final int FIT_8BIT_REGISTER = 0xFF;
	
	@Before
	public void setUp() {
		programCounter = ProgramCounter.getInstance();
		dataRegisterVF = new DataRegister<Integer>();
		register = new DataRegister<Integer>();
		randomNumberGenerator = new RandomNumberGenerator();
		programCounter.write(0x200);
		
		target = new ALU(programCounter, dataRegisterVF, randomNumberGenerator);
	}

	@Test
	public void loadValueIntoRegister() {
		register.write(0x45);
		target.load(register, 0xA);
		
		assertTrue(register.read().equals(0xA));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void addSmallValues() {
		register.write(0x3);
		target.add(register, 0x8);
		
		assertTrue(register.read().equals(0xB));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void addLargeValues() {
		register.write(145);
		target.add(register, 0x3455);
		
		assertTrue(register.read().equals(0xE6));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void addWithRandom() {
		target.addWithRandom(register, 0x2);
		
		assertTrue(register.read().equals(0x2 & randomNumberGenerator.value()));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void addAndDoNotSetCarry() {
		register.write(0x21);
		target.addWithCarry(register, 0x2, FIT_8BIT_REGISTER);
		
		assertTrue(register.read().equals(0x23));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void addAndSetCarry() {
		register.write(0xB0);
		target.addWithCarry(register, 0x3, FIT_8BIT_REGISTER);
		
		assertTrue(register.read().equals(0xB3));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void subtractAndDoNotSetBorrow() {
		register.write(0x3);
		target.subtractWithNegatedBorrow(register, 0x2);
		
		assertTrue(register.read().equals(0xFF));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}

	@Test
	public void subtractAndSetBorrow() {
		register.write(0x2);
		target.subtractWithNegatedBorrow(register, 0x3);
		
		assertTrue(register.read().equals(0x1));
		assertTrue(dataRegisterVF.read().equals(0x1));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void subtractAndSetNegatedBorrow() {
		register.write(0x3);
		target.subtractWithNegatedBorrow(register, 0x2);
		
		assertTrue(register.read().equals(0xFF));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void subtractAndDoNotSetNegatedBorrow() {
		register.write(0x2);
		target.subtractWithNegatedBorrow(register, 0x5);
		
		assertTrue(register.read().equals(0x3));
		assertTrue(dataRegisterVF.read().equals(0x1));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void bitwiseOR() {
		register.write(0x21);
		target.bitwiseOR(register, 0x94);
		
		assertTrue(register.read().equals(0xB5));
		assertTrue(programCounter.read().equals(0x202));
	}

	@Test
	public void bitwiseAND() {
		register.write(0xAC);
		target.bitwiseAND(register, 0xF2);
		
		assertTrue(register.read().equals(0xA0));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void bitwiseXOR() {
		register.write(0xFC);
		target.bitwiseXOR(register, 0x72);
		
		assertTrue(register.read().equals(0x8E));
		assertTrue(programCounter.read().equals(0x202));
	}

	
	@Test
	public void leftShiftWithCarryNotSet() {
		register.write(0x30);
		target.leftShiftWithCarry(register);
		
		assertTrue(register.read().equals(0x60));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}

	@Test
	public void leftShiftWithCarrySet() {
		register.write(0xCE);
		target.leftShiftWithCarry(register);
		
		assertTrue(register.read().equals(0x9C));
		assertTrue(dataRegisterVF.read().equals(0x1));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void rightShiftWithCarryNotSet() {
		register.write(0x30);
		target.rightShiftWithCarry(register);
		
		assertTrue(register.read().equals(0x18));
		assertTrue(dataRegisterVF.read().equals(0x0));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void rightShiftWithCarrySet() {
		register.write(0x21);
		target.rightShiftWithCarry(register);
		
		assertTrue(register.read().equals(0x10));
		assertTrue(dataRegisterVF.read().equals(0x1));
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void doNotskipNextSinceNotEqual() {
		register.write(0x55);
		target.skipNextIfEqual(register, 0x50);
		
		assertTrue(programCounter.read().equals(0x202));
	}
	
	@Test
	public void skipNextBecauseEqual() {
		register.write(0x55);
		target.skipNextIfEqual(register, 0x55);
		
		assertTrue(programCounter.read().equals(0x204));
	}
	
	@Test
	public void skipNextBecauseNotEqual() {
		register.write(0x55);
		target.skipNextIfNotEqual(register, 0x50);
		
		assertTrue(programCounter.read().equals(0x204));
	}
	
	@Test
	public void doNotSkipNextBecauseEqual() {
		register.write(0x55);
		target.skipNextIfNotEqual(register, 0x55);
		
		assertTrue(programCounter.read().equals(0x202));
	}
}
