package joelbits.emu.cpu;

import joelbits.emu.RandomNumberGenerator;
import joelbits.emu.cpu.registers.Register;

public class ALU {
	private Register<Integer> programCounter;
	private Register<Integer> dataRegisterVF;
	private RandomNumberGenerator randomNumberGenerator;
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	
	public ALU(Register<Integer> programCounter, Register<Integer> dataRegisterVF, RandomNumberGenerator randomNumberGenerator) {
		this.programCounter = programCounter;
		this.dataRegisterVF = dataRegisterVF;
		this.randomNumberGenerator = randomNumberGenerator;
	}
	
	public void load(Register<Integer> register, int value) {
		register.write(value);
		incrementProgramCounter(programCounter);
	}
	
	public void add(Register<Integer> register, int value) {
		register.write((register.read() + value) & FIT_8BIT_REGISTER);
		incrementProgramCounter(programCounter);
	}
	
	public void addWithRandom(Register<Integer> register, int value) {
		randomNumberGenerator.generate(FIT_8BIT_REGISTER);
		register.write(randomNumberGenerator.value() & value);
		incrementProgramCounter(programCounter);
	}
	
	public void addWithCarry(Register<Integer> register, int value, int limit) {
		int sum = (register.read() + value) & FIT_16BIT_REGISTER;
		dataRegisterVF.write((sum > limit) ? 1 : 0);
		register.write(limit == FIT_8BIT_REGISTER ? sum & FIT_8BIT_REGISTER : sum);
		incrementProgramCounter(programCounter);
	}
	
	public void subtractWithBorrow(Register<Integer> register, int value) {
		dataRegisterVF.write(register.read() > value ? 1 : 0);
		register.write(convertToUnsignedInt(register.read() - value) & FIT_8BIT_REGISTER);
		incrementProgramCounter(programCounter);
	}
	
	public void subtractWithNegatedBorrow(Register<Integer> register, int value) {
		dataRegisterVF.write(register.read() > value ? 0 : 1);
		register.write(convertToUnsignedInt(value - register.read()) & FIT_8BIT_REGISTER);
		incrementProgramCounter(programCounter);
	}
	
	public void bitwiseOR(Register<Integer> register, int value) {
		register.write(register.read() | value);
		incrementProgramCounter(programCounter);
	}
	
	public void bitwiseAND(Register<Integer> register, int value) {
		register.write(register.read() & value);
		incrementProgramCounter(programCounter);
	}
	
	public void bitwiseXOR(Register<Integer> register, int value) {
		register.write(register.read() ^ value);
		incrementProgramCounter(programCounter);
	}
	
	public void leftShiftWithCarry(Register<Integer> register) {
		dataRegisterVF.write((register.read() >> 7) & 0x1);
		register.write((register.read() << 1) & FIT_8BIT_REGISTER);
		incrementProgramCounter(programCounter);
	}
	
	public void rightShiftWithCarry(Register<Integer> register) {
		dataRegisterVF.write(register.read() & 0x1);
		register.write((register.read() >> 1));
		incrementProgramCounter(programCounter);
	}
	
	public void skipNextIfEqual(Register<Integer> register, int value) {
		if (register.read().equals(value)) {
			incrementProgramCounter(programCounter);
		}
		incrementProgramCounter(programCounter);
	}
	
	public void skipNextIfNotEqual(Register<Integer> register, int value) {
		if (!register.read().equals(value)) {
			incrementProgramCounter(programCounter);
		}
		incrementProgramCounter(programCounter);
	}
	
	private void incrementProgramCounter(Register<Integer> programCounter) {
		programCounter.write((programCounter.read() + 2) & FIT_16BIT_REGISTER);
	}
	
	private int convertToUnsignedInt(int value) {
		return value < 0 ? value + 65536 : value;
	}
}
