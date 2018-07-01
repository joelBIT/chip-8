package joelbits.emulator.cpu;

import joelbits.emulator.cpu.registers.Register;
import joelbits.emulator.utils.RandomNumberGenerator;

public class ALU {
	private final Register<Integer> programCounter;
	private final Register<Integer> dataRegisterVF;
	private final RandomNumberGenerator randomNumberGenerator;
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	
	public ALU(Register<Integer> programCounter, Register<Integer> dataRegisterVF, RandomNumberGenerator randomNumberGenerator) {
		this.programCounter = programCounter;
		this.dataRegisterVF = dataRegisterVF;
		this.randomNumberGenerator = randomNumberGenerator;
	}
	
	public void load(Register<Integer> register, int value) {
		register.write(value);
		incrementProgramCounter();
	}
	
	public void add(Register<Integer> register, int value) {
		register.write((register.read() + value) & FIT_8BIT_REGISTER);
		incrementProgramCounter();
	}
	
	public void addWithRandom(Register<Integer> register, int value) {
		randomNumberGenerator.generate(FIT_8BIT_REGISTER);
		register.write(randomNumberGenerator.value() & value);
		incrementProgramCounter();
	}
	
	public void addWithCarry(Register<Integer> register, int value, int limit) {
		int sum = (register.read() + value) & FIT_16BIT_REGISTER;
		dataRegisterVF.write((sum > limit) ? 1 : 0);
		register.write(limit == FIT_8BIT_REGISTER ? sum & FIT_8BIT_REGISTER : sum);
		incrementProgramCounter();
	}
	
	public void subtractWithBorrow(Register<Integer> register, int value) {
		dataRegisterVF.write(register.read() > value ? 1 : 0);
		register.write(convertToUnsignedInt(register.read() - value) & FIT_8BIT_REGISTER);
		incrementProgramCounter();
	}
	
	public void subtractWithNegatedBorrow(Register<Integer> register, int value) {
		dataRegisterVF.write(register.read() > value ? 0 : 1);
		register.write(convertToUnsignedInt(value - register.read()) & FIT_8BIT_REGISTER);
		incrementProgramCounter();
	}
	
	public void bitwiseOR(Register<Integer> register, int value) {
		register.write((register.read() | value));
		incrementProgramCounter();
	}
	
	public void bitwiseAND(Register<Integer> register, int value) {
		register.write((register.read() & value));
		incrementProgramCounter();
	}
	
	public void bitwiseXOR(Register<Integer> register, int value) {
		register.write((register.read() ^ value));
		incrementProgramCounter();
	}
	
	public void leftShiftWithCarry(Register<Integer> register) {
		dataRegisterVF.write((register.read() >> 7) & 0x1);
		register.write((register.read() << 1) & FIT_8BIT_REGISTER);
		incrementProgramCounter();
	}
	
	public void rightShiftWithCarry(Register<Integer> register) {
		dataRegisterVF.write(register.read() & 0x1);
		register.write((register.read() >> 1));
		incrementProgramCounter();
	}
	
	public void skipNextIfEqual(Register<Integer> register, int value) {
		if (register.read().equals(value)) {
			incrementProgramCounter();
		}
		incrementProgramCounter();
	}
	
	public void skipNextIfNotEqual(Register<Integer> register, int value) {
		if (!register.read().equals(value)) {
			incrementProgramCounter();
		}
		incrementProgramCounter();
	}

	void setProgramCounter(int address) {
		programCounter.write(address);
	}

	public int programCounter() {
		return programCounter.read();
	}

	private void incrementProgramCounter() {
		programCounter.write((programCounter.read() + 2) & FIT_16BIT_REGISTER);
	}

	private int convertToUnsignedInt(int value) {
		return value < 0 ? value + 65536 : value;
	}
}
