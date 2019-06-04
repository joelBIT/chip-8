package joelbits.emulator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import joelbits.emulator.cpu.ALU;
import joelbits.emulator.cpu.registers.DataRegister;
import joelbits.emulator.cpu.registers.ProgramCounter;
import joelbits.emulator.cpu.registers.Register;
import joelbits.emulator.utils.RandomNumberGenerator;

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
		dataRegisterVF = new DataRegister<>();
		register = new DataRegister<>();
		randomNumberGenerator = new RandomNumberGenerator();
		programCounter.write(0x200);

		target = new ALU(programCounter, dataRegisterVF, randomNumberGenerator);
	}

	@Test
	public void loadValueIntoRegister() {
		register.write(0x45);
		target.load(register, 0xA);

		assertEquals(0xA, (int) register.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void addSmallValues() {
		register.write(0x3);
		target.add(register, 0x8);

		assertEquals(0xB, (int) register.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void addLargeValues() {
		register.write(145);
		target.add(register, 0x3455);

		assertEquals(0xE6, (int) register.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void addWithRandom() {
		target.addWithRandom(register, 0x2);

		assertEquals((int) register.read(), 0x2 & randomNumberGenerator.getValue());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void addAndDoNotSetCarry() {
		register.write(0x21);
		target.addWithCarry(register, 0x2, FIT_8BIT_REGISTER);

		assertEquals(0x23, (int) register.read());
		assertEquals(0x0, (int) dataRegisterVF.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void addAndSetCarry() {
		register.write(0xB0);
		target.addWithCarry(register, 0x3, FIT_8BIT_REGISTER);

		assertEquals(0xB3, (int) register.read());
		assertEquals(0x0, (int) dataRegisterVF.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void subtractAndDoNotSetBorrow() {
		register.write(0x3);
		target.subtractWithNegatedBorrow(register, 0x2);

		assertEquals(0xFF, (int) register.read());
		assertEquals(0x0, (int) dataRegisterVF.read());
		assertEquals(0x202, (int) programCounter.read());
	}

	@Test
	public void subtractAndSetBorrow() {
		register.write(0x2);
		target.subtractWithNegatedBorrow(register, 0x3);

		assertEquals(0x1, (int) register.read());
		assertEquals(0x1, (int) dataRegisterVF.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void subtractAndSetNegatedBorrow() {
		register.write(0x3);
		target.subtractWithNegatedBorrow(register, 0x2);

		assertEquals(0xFF, (int) register.read());
		assertEquals(0x0, (int) dataRegisterVF.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void subtractAndDoNotSetNegatedBorrow() {
		register.write(0x2);
		target.subtractWithNegatedBorrow(register, 0x5);

		assertEquals(0x3, (int) register.read());
		assertEquals(0x1, (int) dataRegisterVF.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void bitwiseOR() {
		register.write(0x21);
		target.bitwiseOR(register, 0x94);

		assertEquals(0xB5, (int) register.read());
		assertEquals(0x202, (int) programCounter.read());
	}

	@Test
	public void bitwiseAND() {
		register.write(0xAC);
		target.bitwiseAND(register, 0xF2);

		assertEquals(0xA0, (int) register.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void bitwiseXOR() {
		register.write(0xFC);
		target.bitwiseXOR(register, 0x72);

		assertEquals(0x8E, (int) register.read());
		assertEquals(0x202, (int) programCounter.read());
	}

	
	@Test
	public void leftShiftWithCarryNotSet() {
		register.write(0x30);
		target.leftShiftWithCarry(register);

		assertEquals(0x60, (int) register.read());
		assertEquals(0x0, (int) dataRegisterVF.read());
		assertEquals(0x202, (int) programCounter.read());
	}

	@Test
	public void leftShiftWithCarrySet() {
		register.write(0xCE);
		target.leftShiftWithCarry(register);

		assertEquals(0x9C, (int) register.read());
		assertEquals(0x1, (int) dataRegisterVF.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void rightShiftWithCarryNotSet() {
		register.write(0x30);
		target.rightShiftWithCarry(register);

		assertEquals(0x18, (int) register.read());
		assertEquals(0x0, (int) dataRegisterVF.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void rightShiftWithCarrySet() {
		register.write(0x21);
		target.rightShiftWithCarry(register);

		assertEquals(0x10, (int) register.read());
		assertEquals(0x1, (int) dataRegisterVF.read());
		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void doNotskipNextSinceNotEqual() {
		register.write(0x55);
		target.skipNextIfEqual(register, 0x50);

		assertEquals(0x202, (int) programCounter.read());
	}
	
	@Test
	public void skipNextBecauseEqual() {
		register.write(0x55);
		target.skipNextIfEqual(register, 0x55);

		assertEquals(0x204, (int) programCounter.read());
	}
	
	@Test
	public void skipNextBecauseNotEqual() {
		register.write(0x55);
		target.skipNextIfNotEqual(register, 0x50);

		assertEquals(0x204, (int) programCounter.read());
	}
	
	@Test
	public void doNotSkipNextBecauseEqual() {
		register.write(0x55);
		target.skipNextIfNotEqual(register, 0x55);

		assertEquals(0x202, (int) programCounter.read());
	}
}
