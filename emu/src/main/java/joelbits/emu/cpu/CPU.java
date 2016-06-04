package joelbits.emu.cpu;

import java.util.List;
import java.util.Random;
import java.util.Stack;

import joelbits.emu.Screen;
import joelbits.emu.cpu.registers.IndexRegister;
import joelbits.emu.cpu.registers.InstructionRegister;
import joelbits.emu.cpu.registers.ProgramCounter;
import joelbits.emu.cpu.registers.Register;
import joelbits.emu.input.Keyboard;
import joelbits.emu.memory.Memory;
import joelbits.emu.output.Buffer;
import joelbits.emu.output.BufferFactory;
import joelbits.emu.output.Sound;

/**
 * There are 16 data registers named from V0 to VF. The carry flag (VF) is set to 1 if any screen pixels are flipped from set 
 * to unset when a sprite is drawn and set to 0 otherwise. Last 8 bits of each register are used to represent an unsigned byte.
 * In this CPU implementation components like the control unit (CU) and the arithmetic-logic unit (ALU) are abstracted away.
 *
 */
public class CPU {
	private MemoryBus memoryBus = new MemoryBus();
	private final ExpansionBus expansionBus = new ExpansionBus();
	private final Buffer displayBuffer = BufferFactory.createDisplayBuffer(getScreen().width(), getScreen().height());
	private final Buffer dirtyBuffer = BufferFactory.createDirtyBuffer();
	private final Stack<Integer> stack = new Stack<Integer>();
	private Timer<Integer> delayTimer;
	private Timer<Integer> soundTimer;
	private Flag drawFlag;
	private Flag clearFlag;
	private final List<Register<Integer>> dataRegisters;
	private final InstructionRegister<Integer> instructionRegister;
	private final ProgramCounter<Integer> programCounter;
	private final IndexRegister<Integer> indexRegister;
	
	private int registerLocationX;
	private int registerLocationY;
	private int nibble;
	private int address;
	private int lowestByte;
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	private int randomNumber;		// For testing purposes
	
	public CPU(List<Register<Integer>> dataRegisters, InstructionRegister<Integer> instructionRegister, ProgramCounter<Integer> programCounter, IndexRegister<Integer> indexRegister, List<Timer<Integer>> timers, List<Flag> flags) {
		this.dataRegisters = dataRegisters;
		this.instructionRegister = instructionRegister;
		this.programCounter = programCounter;
		this.indexRegister = indexRegister;
		assignTimers(timers);
		assignFlags(flags);
	}
	
	private void assignTimers(List<Timer<Integer>> timers) {
		String className;
		for (Timer<Integer> timer : timers) {
			className = timer.getClass().getName();
			if (className.equals("joelbits.emu.cpu.SoundTimer")) {
				soundTimer = timer;
			} else if (className.equals("joelbits.emu.cpu.DelayTimer")) {
				delayTimer = timer;
			}
		}
	}
	
	private void assignFlags(List<Flag> flags) {
		String className;
		for (Flag flag : flags) {
			className = flag.getClass().getName();
			if (className.equals("joelbits.emu.cpu.ClearFlag")) {
				clearFlag = flag;
			} else if (className.equals("joelbits.emu.cpu.DrawFlag")) {
				drawFlag = flag;
			}
		}
	}
	
	public Keyboard getKeyboard() {
		return expansionBus.getKeyboard();
	}
	
	public Buffer getDisplayBuffer() {
		return displayBuffer;
	}
	
	public Buffer getDirtyBuffer() {
		return dirtyBuffer;
	}
	
	public Sound getSound() {
		return expansionBus.getSound();
	}
	
	public Screen<Integer> getScreen() {
		return expansionBus.getScreen();
	}
	
	public Memory getMemory() {
		return memoryBus.getPrimaryMemory();
	}
	
	public void initialize(int address, int instruction, int index, int delayTime, int soundTime, int[] data) {
		programCounter.write(address);
		delayTimer.setValue(delayTime);
		soundTimer.setValue(soundTime);
		indexRegister.write(index);
		instructionRegister.write(instruction);
		
		getDirtyBuffer().clear();
		getDisplayBuffer().clear();
		getMemory().clear();
		
		for (int i = 0; i < data.length; i++) {
			getMemory().write(i, data[i]);
		}
	}
	
	public void resetDataRegisters() {
		for (int i = 0; i < dataRegisters.size(); i++) {
			dataRegisters.get(i).write(0);
		}
	}
	
	public void loadROM(byte[] ROM, int startLocation) {
		for (int i = 0, location = startLocation; i < ROM.length; i++, location++) {
			getMemory().write(location, Byte.toUnsignedInt(ROM[i]));
		}
	}
	
	public void nextInstructionCycle() {
		int instruction = getMemory().read(programCounter.read()) << 8 | getMemory().read(programCounter.read()+1);
		instructionRegister.write(Integer.valueOf(instruction));
		
		registerLocationX = (instruction & 0x0F00) >> 8;
		registerLocationY = (instruction & 0x00F0) >> 4;
		nibble = instruction & 0x000F;
		address = instruction & 0x0FFF;
		lowestByte = instruction & 0x00FF;
		
		switch(Integer.toHexString(instruction & 0xF000).toUpperCase()) {
			case "0":
				switch(Integer.toHexString(address).toUpperCase()) {
					case "E0":
						getDisplayBuffer().clear();
						getDirtyBuffer().clear();
						if (!clearFlag.isActive()) {
							clearFlag.toggle();
						}
						programCounter.write(programCounter.read() + 2);
						break;
					case "EE":
						programCounter.write(stack.pop() + 2);
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instruction & FIT_16BIT_REGISTER) + " at (0) location " + programCounter.read());
						break;
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
				programCounter.write(programCounter.read() + ((dataRegisters.get(registerLocationX).read().equals(lowestByte)) ? 4 : 2));
				break;
			case "4000":
				programCounter.write(programCounter.read() + ((dataRegisters.get(registerLocationX).read() != lowestByte) ? 4 : 2));
				break;
			case "5000":
				programCounter.write(programCounter.read() + ((dataRegisters.get(registerLocationX).read().equals(dataRegisters.get(registerLocationY).read()) ? 4 : 2)));
				break;
			case "6000":
				dataRegisters.get(registerLocationX).write(lowestByte);
				programCounter.write(programCounter.read() + 2);
				break;
			case "7000":
				dataRegisters.get(registerLocationX).write((dataRegisters.get(registerLocationX).read() + lowestByte) & FIT_8BIT_REGISTER);
				programCounter.write(programCounter.read() + 2);
				break;
			case "8000":
				switch(Integer.toHexString(nibble).toUpperCase()) {
					case "0":
						dataRegisters.get(registerLocationX).write(dataRegisters.get(registerLocationY).read());
						programCounter.write(programCounter.read() + 2);
						break;
					case "1":
						dataRegisters.get(registerLocationX).write((dataRegisters.get(registerLocationX).read() | dataRegisters.get(registerLocationY).read()));
						programCounter.write(programCounter.read() + 2);
						break;
					case "2":
						dataRegisters.get(registerLocationX).write((dataRegisters.get(registerLocationX).read() & dataRegisters.get(registerLocationY).read()));
						programCounter.write(programCounter.read() + 2);
						break;
					case "3":
						dataRegisters.get(registerLocationX).write((dataRegisters.get(registerLocationX).read() ^ dataRegisters.get(registerLocationY).read()));
						programCounter.write(programCounter.read() + 2);
						break;
					case "4":
						int sum = dataRegisters.get(registerLocationX).read() + dataRegisters.get(registerLocationY).read();
						dataRegisters.get(0xF).write(((sum > FIT_8BIT_REGISTER) ? 1 : 0));
						dataRegisters.get(registerLocationX).write(sum & FIT_8BIT_REGISTER);
						programCounter.write(programCounter.read() + 2);
						break;
					case "5":
						dataRegisters.get(0xF).write((dataRegisters.get(registerLocationX).read() > dataRegisters.get(registerLocationY).read()) ? 1 : 0);
						dataRegisters.get(registerLocationX).write(convertToUnsignedInt(dataRegisters.get(registerLocationX).read() - dataRegisters.get(registerLocationY).read()) & FIT_8BIT_REGISTER);
						programCounter.write(programCounter.read() + 2);
						break;
					case "6":
						dataRegisters.get(0xF).write(dataRegisters.get(registerLocationX).read() & 0x1);
						dataRegisters.get(registerLocationX).write((dataRegisters.get(registerLocationX).read() >> 1) & FIT_8BIT_REGISTER);
						programCounter.write(programCounter.read() + 2);
						break;
					case "7":
						dataRegisters.get(0xF).write(((dataRegisters.get(registerLocationX).read() > dataRegisters.get(registerLocationY).read()) ? 0 : 1));
						dataRegisters.get(registerLocationX).write(convertToUnsignedInt(dataRegisters.get(registerLocationY).read() - dataRegisters.get(registerLocationX).read()) & FIT_8BIT_REGISTER);
						programCounter.write(programCounter.read() + 2);
						break;
					case "E":
						dataRegisters.get(0xF).write((dataRegisters.get(registerLocationX).read() >> 7) & 0x1);
						dataRegisters.get(registerLocationX).write((dataRegisters.get(registerLocationX).read() << 1) & FIT_8BIT_REGISTER);
						programCounter.write(programCounter.read() + 2);
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instruction & FIT_16BIT_REGISTER) + " at (8) location " + programCounter.read());
						break;
				}
				break;
			case "9000":
				programCounter.write(programCounter.read() + ((dataRegisters.get(registerLocationX).read() != dataRegisters.get(registerLocationY).read()) ? 4 : 2));
				break;
			case "A000":
				indexRegister.write(address);
				programCounter.write(programCounter.read() + 2);
				break;
			case "B000":
				programCounter.write(dataRegisters.get(0x0).read() + address);
				break;
			case "C000":
				randomNumber = new Random().nextInt(FIT_8BIT_REGISTER);
				dataRegisters.get(registerLocationX).write(randomNumber & lowestByte);
				programCounter.write(programCounter.read() + 2);
				break;
			case "D000":
				dataRegisters.get(0xF).write(0);
				for (int row = 0; row < nibble; row++) {
					int memoryByte = getMemory().read(indexRegister.read() + row);
					int coordinateY = dataRegisters.get(registerLocationY).read() + row;
					for (int column = 0; column < 8; column++) {
						if ((memoryByte & (0x80 >> column)) != 0) {
							int coordinateX = dataRegisters.get(registerLocationX).read() + column;
							int data = getDisplayBuffer().read(convertToIndex(coordinateX, coordinateY));
							if (data != 0) {
								dataRegisters.get(0xF).write(1);
							} 
							getDisplayBuffer().write(convertToIndex(coordinateX, coordinateY), data^1);
							getDirtyBuffer().write(convertToIndex(coordinateX, coordinateY), data^1);
						}
					}
				}
				if (!drawFlag.isActive()) {
					drawFlag.toggle();
				}
				programCounter.write(programCounter.read() + 2);
				break;
			case "E000":
				switch(Integer.toHexString(lowestByte).toUpperCase()) {
					case "9E":
						programCounter.write(programCounter.read() + (getKeyboard().getCurrentlyPressedKey() == dataRegisters.get(registerLocationX).read() ? 4 : 2));
						break;
					case "A1":
						programCounter.write(programCounter.read() + (getKeyboard().getCurrentlyPressedKey() != dataRegisters.get(registerLocationX).read() ? 4 : 2));
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instruction & FIT_16BIT_REGISTER) + " at (E) location " + programCounter.read());
						break;
				}
				break;
			case "F000":
				switch(Integer.toHexString(lowestByte).toUpperCase()) {
					case "7":
						dataRegisters.get(registerLocationX).write(delayTimer.currentValue());
						programCounter.write(programCounter.read() + 2);
						break;
					case "A":
						while (getKeyboard().getCurrentlyPressedKey() == 0) {
							;
						}
						dataRegisters.get(registerLocationX).write(getKeyboard().getCurrentlyPressedKey());
						programCounter.write(programCounter.read() + 2);
						break;
					case "15":
						delayTimer.setValue(dataRegisters.get(registerLocationX).read());
						programCounter.write(programCounter.read() + 2);
						break;
					case "18":
						soundTimer.setValue(dataRegisters.get(registerLocationX).read().equals(1) ? 2 : dataRegisters.get(registerLocationX).read());
						programCounter.write(programCounter.read() + 2);
						break;
					case "1E":
						int sum = (indexRegister.read() + dataRegisters.get(registerLocationX).read()) & FIT_16BIT_REGISTER;
						dataRegisters.get(0xF).write(sum > 0xFFF ? 1 : 0);
						indexRegister.write(sum);
						programCounter.write(programCounter.read() + 2);
						break;
					case "29":
						indexRegister.write((dataRegisters.get(registerLocationX).read() * 5) & FIT_16BIT_REGISTER);
				 		programCounter.write(programCounter.read() + 2);
						break;
					case "33":
				 		getMemory().write(indexRegister.read(), dataRegisters.get(registerLocationX).read() / 100);
				 		getMemory().write(indexRegister.read() + 1, (dataRegisters.get(registerLocationX).read() % 100) / 10);
				 		getMemory().write(indexRegister.read() + 2, dataRegisters.get(registerLocationX).read() % 10);
				 		programCounter.write(programCounter.read() + 2);
						break;
					case "55":
						for (int i = 0; i <= registerLocationX; i++) {
							getMemory().write(indexRegister.read() + i, dataRegisters.get(i).read());
						}
						programCounter.write(programCounter.read() + 2);
						break;
					case "65":
						for (int i = 0; i <= registerLocationX; i++) {
							dataRegisters.get(i).write(getMemory().read(indexRegister.read() + i));
						}
						programCounter.write(programCounter.read() + 2);
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instruction & FIT_16BIT_REGISTER) + " at (f) location " + programCounter.read());
						break;
				}
				break;
			default:
				System.out.println("Unknown instruction " + Integer.toHexString(instruction & FIT_16BIT_REGISTER) + " at (all) location " + programCounter.read());
				break;
		}
		programCounter.write(programCounter.read() & FIT_16BIT_REGISTER);
	}
	
	private int convertToUnsignedInt(int value) {
		return value < 0 ? value + 65536 : value;
	}
	
	private int convertToIndex(int coordinateX, int coordinateY) {
		return (coordinateX % getScreen().width()) + ((coordinateY % getScreen().width()) * getScreen().width());
	}
	
	public int readRandomNumber() {
		return randomNumber;
	}
	
	public int readStackTopValue() {
		return stack.isEmpty() ? -1 : stack.peek();
	}
}
