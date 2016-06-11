package joelbits.emu.cpu;

import java.util.List;
import java.util.Stack;

import javafx.scene.input.KeyCode;
import joelbits.emu.cpu.registers.Register;
import joelbits.emu.input.Input;
import joelbits.emu.memory.Memory;
import joelbits.emu.timers.Timer;

/**
 * There are 16 data registers named from V0 to VF. The carry flag (VF) is set to 1 if any screen pixels are flipped from set 
 * to unset when a sprite is drawn and set to 0 otherwise. Last 8 bits of each register are used to represent an unsigned byte.
 * In this CPU implementation components like the control unit (CU) and the arithmetic-logic unit (ALU) are abstracted away.
 *
 */
public class CPU {
	private final Memory primaryMemory;
	private final Input<Integer, KeyCode> keyboard;
	private final Stack<Integer> stack = new Stack<Integer>();
	private final List<Register<Integer>> dataRegisters;
	private final Register<Integer> instructionRegister;
	private final Register<Integer> programCounter;
	private final Register<Integer> indexRegister;
	private final ALU alu;
	private final GPU gpu;
	private Timer<Integer> delayTimer;
	private Timer<Integer> soundTimer;
	
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	private int registerLocationX;
	private int registerLocationY;
	private int nibble;
	private int address;
	private int lowestByte;
	
	public CPU(Memory primaryMemory, Input<Integer, KeyCode> keyboard, List<Register<Integer>> dataRegisters, Register<Integer> instructionRegister, Register<Integer> programCounter, Register<Integer> indexRegister, Timer<Integer> delayTimer, Timer<Integer> soundTimer, ALU alu, GPU gpu) {
		this.primaryMemory = primaryMemory;
		this.keyboard = keyboard;
		this.dataRegisters = dataRegisters;
		this.instructionRegister = instructionRegister;
		this.programCounter = programCounter;
		this.indexRegister = indexRegister;
		this.delayTimer = delayTimer;
		this.soundTimer = soundTimer;
		this.alu = alu;
		this.gpu = gpu;
	}
	
	public void initialize(int address, int instruction, int index, int delayTime, int soundTime, int[] data) {
		programCounter.write(address);
		delayTimer.setValue(delayTime);
		soundTimer.setValue(soundTime);
		indexRegister.write(index);
		instructionRegister.write(instruction);
		
		gpu.clearBuffers();
		primaryMemory.clear();
		
		for (int i = 0; i < data.length; i++) {
			primaryMemory.write(i, data[i]);
		}
	}
	
	public void resetDataRegisters() {
		for (int i = 0; i < dataRegisters.size(); i++) {
			dataRegisters.get(i).write(0);
		}
	}
	
	public void loadROM(byte[] ROM, int startLocation) {
		for (int i = 0, location = startLocation; i < ROM.length; i++, location++) {
			primaryMemory.write(location, Byte.toUnsignedInt(ROM[i]));
		}
	}
	
	public void executeNextOperation() {
		int instruction = fetchNextInstruction();
		extractInstructionInformation(instruction);
		
		String leastSignificantNibble = Integer.toHexString(nibble).toUpperCase();
		String leastSignificantByte = Integer.toHexString(lowestByte).toUpperCase();
		
		switch(Integer.toHexString(instruction & 0xF000).toUpperCase()) {
			case "0":
				if (Integer.toHexString(lowestByte).toUpperCase().equals("E0")) {
					gpu.clearBuffers();
					programCounter.write(programCounter.read() + 2);
				} else if (Integer.toHexString(lowestByte).toUpperCase().equals("EE")) {
					programCounter.write(stack.pop() + 2);
				}
				break;
			case "1000":
				programCounter.write(address);
				break;
			case "2000":
				stack.push(programCounter.read());
				programCounter.write(address);
				break;
			case "3000":
				alu.skipNextIfEqual(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case "4000":
				alu.skipNextIfNotEqual(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case "5000":
				alu.skipNextIfEqual(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case "6000":
				alu.load(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case "7000":
				alu.add(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case "8000":
				if (leastSignificantNibble.equals("0")) {
					alu.load(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("1")) {
					alu.bitwiseOR(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("2")) {
					alu.bitwiseAND(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("3")) {
					alu.bitwiseXOR(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("4")) {
					alu.addWithCarry(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read(), FIT_8BIT_REGISTER);
				} else if (leastSignificantNibble.equals("5")) {
					alu.subtractWithBorrow(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("6")) {
					alu.rightShiftWithCarry(dataRegisters.get(registerLocationX));
				} else if (leastSignificantNibble.equals("7")) {
					alu.subtractWithNegatedBorrow(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("E")) {
					alu.leftShiftWithCarry(dataRegisters.get(registerLocationX));
				}
				break;
			case "9000":
				alu.skipNextIfNotEqual(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case "A000":
				alu.load(indexRegister, address);
				break;
			case "B000":
				programCounter.write(dataRegisters.get(0x0).read() + address);
				break;
			case "C000":
				alu.addWithRandom(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case "D000":
				gpu.drawSprite(dataRegisters, primaryMemory, indexRegister, instruction);
				programCounter.write(programCounter.read() + 2);
				break;
			case "E000":
				if (leastSignificantByte.equals("9E")) {
					alu.skipNextIfEqual(dataRegisters.get(registerLocationX), keyboard.currentlyPressed());
				} else if (leastSignificantByte.equals("A1")) {
					alu.skipNextIfNotEqual(dataRegisters.get(registerLocationX), keyboard.currentlyPressed());
				}
				break;
			case "F000":
				if (leastSignificantNibble.equals("7")) {
					alu.load(dataRegisters.get(registerLocationX), delayTimer.currentValue());
				} else if (leastSignificantNibble.equals("A")) {
					while (keyboard.currentlyPressed().equals(0)) {
						;
					}
					alu.load(dataRegisters.get(registerLocationX), keyboard.currentlyPressed());
				} else if (leastSignificantByte.equals("15")) {
					delayTimer.setValue(dataRegisters.get(registerLocationX).read());
					programCounter.write(programCounter.read() + 2);
				} else if (leastSignificantByte.equals("18")) {
					int value = dataRegisters.get(registerLocationX).read();
					soundTimer.setValue(value == 1 ? 2 : value);
					programCounter.write(programCounter.read() + 2);
				} else if (leastSignificantByte.equals("1E")) {
					alu.addWithCarry(indexRegister, dataRegisters.get(registerLocationX).read(), 0xFFF);
				} else if (leastSignificantByte.equals("29")) {
					alu.load(indexRegister, (dataRegisters.get(registerLocationX).read() * 5) & FIT_16BIT_REGISTER);
				} else if (leastSignificantByte.equals("33")) {
					writeBcdRepresentationToMemory(registerLocationX);
			 		programCounter.write(programCounter.read() + 2);
				} else if (leastSignificantByte.equals("55")) {
					writeDataRegistersToMemory(registerLocationX);
					programCounter.write(programCounter.read() + 2);
				} else if (leastSignificantByte.equals("65")) {
					writeMemoryToDataRegisters(registerLocationX);
					programCounter.write(programCounter.read() + 2);
				}
				break;
			default:
				System.out.println("Unknown instruction " + Integer.toHexString(instruction & FIT_16BIT_REGISTER) + " at (all) location " + programCounter.read());
				break;
		}
	}
	
	private int fetchNextInstruction() {
		int instruction = primaryMemory.read(programCounter.read()) << 8 | primaryMemory.read(programCounter.read()+1);
		instructionRegister.write(Integer.valueOf(instruction));
		return instruction;
	}
	
	private void extractInstructionInformation(int instruction) {
		registerLocationX = (instruction & 0x0F00) >> 8;
		registerLocationY = (instruction & 0x00F0) >> 4;
		nibble = instruction & 0x000F;
		address = instruction & 0x0FFF;
		lowestByte = instruction & 0x00FF;
	}
	
	private void writeBcdRepresentationToMemory(int registerLocation) {
		primaryMemory.write(indexRegister.read(), dataRegisters.get(registerLocation).read() / 100);
 		primaryMemory.write(indexRegister.read() + 1, (dataRegisters.get(registerLocation).read() % 100) / 10);
 		primaryMemory.write(indexRegister.read() + 2, dataRegisters.get(registerLocation).read() % 10);
	}
	
	private void writeDataRegistersToMemory(int registerBound) {
		for (int i = 0; i <= registerBound; i++) {
			primaryMemory.write(indexRegister.read() + i, dataRegisters.get(i).read());
		}
	}
	
	private void writeMemoryToDataRegisters(int registerBound) {
		for (int i = 0; i <= registerBound; i++) {
			dataRegisters.get(i).write(primaryMemory.read(indexRegister.read() + i));
		}
	}
	
	public int readStackTopValue() {
		return stack.isEmpty() ? -1 : stack.peek();
	}
}
