package joelbits.emu.cpu;

import java.util.ArrayList;
import java.util.List;

/**
 * There are 16 data registers named from V0 to VF. The carry flag (VF) is set to 1 if any screen pixels are flipped from set 
 * to unset when a sprite is drawn and set to 0 otherwise. Last 8 bits of each register are used to represent an unsigned byte.
 * 
 */
public class Registers<T> {
	private List<T> dataRegisters = new ArrayList<T>();
	private T instructionRegister;
	private T indexRegister;
	private T programCounter;
	
	public Registers(int numberOfRegisters, T value) {
		for (int i = 0; i <= numberOfRegisters; i++) {
			dataRegisters.add(value);
		}
	}
	
	public void writeInstructionRegister(T instruction) {
		instructionRegister = instruction;
	}
	
	public T readInstructionRegister() {
		return instructionRegister;
	}
	
	public void writeDataRegister(int dataRegister, T data) {
		dataRegisters.set(dataRegister, data);
	}
	
	public T readDataRegister(int dataRegister) {
		return dataRegisters.get(dataRegister);
	}
	
	public void writeIndexRegister(T value) {
		indexRegister = value;
	}
	
	public T readIndexRegister() {
		return indexRegister;
	}
	
	public void writeProgramCounter(T address) {
		programCounter = address;
	}
	
	public T readProgramCounter() {
		return programCounter;
	}
}
