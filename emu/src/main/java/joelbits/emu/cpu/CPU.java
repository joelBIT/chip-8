package joelbits.emu.cpu;

import java.util.Random;
import java.util.Stack;

import joelbits.emu.Screen;
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
	private final Registers<Integer> registers = new Registers<Integer>(0xF, 0);
	private final Timer<Integer> delayTimer = new DelayTimer<Integer>();
	private final Timer<Integer> soundTimer = new SoundTimer<Integer>();
	private boolean drawFlag;
	private boolean clearFlag;
	private int registerLocationX;
	private int registerLocationY;
	private int nibble;
	private int address;
	private int lowestByte;
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	private int randomNumber;		// For testing purposes
	
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
	
	public boolean isDrawFlag() {
		return drawFlag;
	}
	
	public void toggleDrawFlag() {
		drawFlag = !drawFlag;
	}
	
	public boolean isClearFlag() {
		return clearFlag;
	}
	
	public void toggleClearFlag() {
		clearFlag = !clearFlag;
	}
	
	public synchronized void decrementDelayTimer() {
		if (delayTimer.currentValue() > 0) {
			delayTimer.setValue(delayTimer.currentValue() - 1);;
		}
	}
	
	public synchronized void decrementSoundTimer() {
		soundTimer.setValue(soundTimer.currentValue() - 1);
		if (soundTimer.currentValue() <= 0) {
			getSound().stop();
		}
	}
	
	public void initialize(int programCounter, int instructionRegister, int indexRegister, int delayTimer, int soundTimer, int[] fontset) {
		registers.writeProgramCounter(programCounter);
		registers.writeInstructionRegister(instructionRegister);
		registers.writeIndexRegister(indexRegister);
		this.delayTimer.setValue(delayTimer);
		this.soundTimer.setValue(soundTimer);
		getMemory().clear();
		getDirtyBuffer().clear();
		getDisplayBuffer().clear();
		getMemory().clear();
		for (int i = 0; i < fontset.length; i++) {
			getMemory().write(i, fontset[i]);
		}
	}
	
	public void loadROM(byte[] ROM, int startLocation) {
		for (int i = 0, location = startLocation; i < ROM.length; i++, location++) {
			getMemory().write(location, Byte.toUnsignedInt(ROM[i]));
		}
	}
	
	public void nextInstructionCycle() {
		int instructionRegister = getMemory().read(registers.readProgramCounter()) << 8 | getMemory().read(registers.readProgramCounter()+1);
		registers.writeInstructionRegister(instructionRegister);
		
		registerLocationX = (instructionRegister & 0x0F00) >> 8;
		registerLocationY = (instructionRegister & 0x00F0) >> 4;
		nibble = instructionRegister & 0x000F;
		address = instructionRegister & 0x0FFF;
		lowestByte = instructionRegister & 0x00FF;
		
		switch(Integer.toHexString(instructionRegister & 0xF000).toUpperCase()) {
			case "0":
				switch(Integer.toHexString(address).toUpperCase()) {
					case "E0":
						getDisplayBuffer().clear();
						getDirtyBuffer().clear();
						clearFlag = true;
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "EE":
						registers.writeProgramCounter(stack.pop() + 2);
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (0) location " + registers.readProgramCounter());
						break;
				}
				break;
			case "1000":
				registers.writeProgramCounter(address);
				break;
			case "2000":
				stack.push(registers.readProgramCounter());
				registers.writeProgramCounter(address);
				break;
			case "3000":
				registers.writeProgramCounter(registers.readProgramCounter() + ((registers.readDataRegister(registerLocationX).equals(lowestByte)) ? 4 : 2));
				break;
			case "4000":
				registers.writeProgramCounter(registers.readProgramCounter() + ((registers.readDataRegister(registerLocationX) != lowestByte) ? 4 : 2));
				break;
			case "5000":
				registers.writeProgramCounter(registers.readProgramCounter() + ((registers.readDataRegister(registerLocationX).equals(registers.readDataRegister(registerLocationY)) ? 4 : 2)));
				break;
			case "6000":
				registers.writeDataRegister(registerLocationX, lowestByte);
				registers.writeProgramCounter(registers.readProgramCounter() + 2);
				break;
			case "7000":
				registers.writeDataRegister(registerLocationX, (registers.readDataRegister(registerLocationX) + lowestByte) & FIT_8BIT_REGISTER);
				registers.writeProgramCounter(registers.readProgramCounter() + 2);
				break;
			case "8000":
				switch(Integer.toHexString(nibble).toUpperCase()) {
					case "0":
						registers.writeDataRegister(registerLocationX, registers.readDataRegister(registerLocationY));
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "1":
						registers.writeDataRegister(registerLocationX, (registers.readDataRegister(registerLocationX) | registers.readDataRegister(registerLocationY)));
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "2":
						registers.writeDataRegister(registerLocationX, (registers.readDataRegister(registerLocationX) & registers.readDataRegister(registerLocationY)));
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "3":
						registers.writeDataRegister(registerLocationX, (registers.readDataRegister(registerLocationX) ^ registers.readDataRegister(registerLocationY)));
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "4":
						int sum = registers.readDataRegister(registerLocationX) + registers.readDataRegister(registerLocationY);
						registers.writeDataRegister(0xF, ((sum > FIT_8BIT_REGISTER) ? 1 : 0));
						registers.writeDataRegister(registerLocationX, sum & FIT_8BIT_REGISTER);
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "5":
						registers.writeDataRegister(0xF, ((registers.readDataRegister(registerLocationX) > registers.readDataRegister(registerLocationY)) ? 1 : 0));
						registers.writeDataRegister(registerLocationX, convertToUnsignedInt(registers.readDataRegister(registerLocationX) - registers.readDataRegister(registerLocationY)) & FIT_8BIT_REGISTER);
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "6":
						registers.writeDataRegister(0xF, registers.readDataRegister(registerLocationX) & 0x1);
						registers.writeDataRegister(registerLocationX, (registers.readDataRegister(registerLocationX) >> 1) & FIT_8BIT_REGISTER);
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "7":
						registers.writeDataRegister(0xF, ((registers.readDataRegister(registerLocationX) > registers.readDataRegister(registerLocationY)) ? 0 : 1));
						registers.writeDataRegister(registerLocationX, convertToUnsignedInt(registers.readDataRegister(registerLocationY) - registers.readDataRegister(registerLocationX)) & FIT_8BIT_REGISTER);
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "E":
						registers.writeDataRegister(0xF, (registers.readDataRegister(registerLocationX) >> 7) & 0x1);
						registers.writeDataRegister(registerLocationX, (registers.readDataRegister(registerLocationX) << 1) & FIT_8BIT_REGISTER);
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (8) location " + registers.readProgramCounter());
						break;
				}
				break;
			case "9000":
				registers.writeProgramCounter(registers.readProgramCounter() + ((registers.readDataRegister(registerLocationX) != registers.readDataRegister(registerLocationY)) ? 4 : 2));
				break;
			case "A000":
				registers.writeIndexRegister(address);
				registers.writeProgramCounter(registers.readProgramCounter() + 2);
				break;
			case "B000":
				registers.writeProgramCounter(registers.readDataRegister(0x0) + address);
				break;
			case "C000":
				randomNumber = new Random().nextInt(FIT_8BIT_REGISTER);
				registers.writeDataRegister(registerLocationX, randomNumber & lowestByte);
				registers.writeProgramCounter(registers.readProgramCounter() + 2);
				break;
			case "D000":
				registers.writeDataRegister(0xF, 0);
				for (int row = 0; row < nibble; row++) {
					int memoryByte = getMemory().read(registers.readIndexRegister() + row);
					int coordinateY = registers.readDataRegister(registerLocationY) + row;
					for (int column = 0; column < 8; column++) {
						if ((memoryByte & (0x80 >> column)) != 0) {
							int coordinateX = registers.readDataRegister(registerLocationX) + column;
							int data = getDisplayBuffer().read(convertToIndex(coordinateX, coordinateY));
							if (data != 0) {
								registers.writeDataRegister(0xF, 1);
							} 
							getDisplayBuffer().write(convertToIndex(coordinateX, coordinateY), data^1);
							getDirtyBuffer().write(convertToIndex(coordinateX, coordinateY), data^1);
						}
					}
				}
				drawFlag = true;
				registers.writeProgramCounter(registers.readProgramCounter() + 2);
				break;
			case "E000":
				switch(Integer.toHexString(lowestByte).toUpperCase()) {
					case "9E":
						registers.writeProgramCounter(registers.readProgramCounter() + (getKeyboard().getCurrentlyPressedKey() == registers.readDataRegister(registerLocationX) ? 4 : 2));
						break;
					case "A1":
						registers.writeProgramCounter(registers.readProgramCounter() + (getKeyboard().getCurrentlyPressedKey() != registers.readDataRegister(registerLocationX) ? 4 : 2));
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (E) location " + registers.readProgramCounter());
						break;
				}
				break;
			case "F000":
				switch(Integer.toHexString(lowestByte).toUpperCase()) {
					case "7":
						registers.writeDataRegister(registerLocationX, delayTimer.currentValue());
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "A":
						while (getKeyboard().getCurrentlyPressedKey() == 0) {
							;
						}
						registers.writeDataRegister(registerLocationX, getKeyboard().getCurrentlyPressedKey());
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "15":
						delayTimer.setValue(registers.readDataRegister(registerLocationX));
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "18":
						soundTimer.setValue(registers.readDataRegister(registerLocationX).equals(1) ? 2 : registers.readDataRegister(registerLocationX));
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "1E":
						int sum = (registers.readIndexRegister() + registers.readDataRegister(registerLocationX)) & FIT_16BIT_REGISTER;
						registers.writeDataRegister(0xF, (sum > 0xFFF ? 1 : 0));
						registers.writeIndexRegister(sum);
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "29":
						registers.writeIndexRegister((registers.readDataRegister(registerLocationX) * 5) & FIT_16BIT_REGISTER);
				 		registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "33":
				 		getMemory().write(registers.readIndexRegister(), registers.readDataRegister(registerLocationX) / 100);
				 		getMemory().write(registers.readIndexRegister() + 1, (registers.readDataRegister(registerLocationX) % 100) / 10);
				 		getMemory().write(registers.readIndexRegister() + 2, registers.readDataRegister(registerLocationX) % 10);
				 		registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "55":
						for (int i = 0; i <= registerLocationX; i++) {
							getMemory().write(registers.readIndexRegister() + i, registers.readDataRegister(i));
						}
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					case "65":
						for (int i = 0; i <= registerLocationX; i++) {
							registers.writeDataRegister(i, getMemory().read(registers.readIndexRegister() + i));
						}
						registers.writeProgramCounter(registers.readProgramCounter() + 2);
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (f) location " + registers.readProgramCounter());
						break;
				}
				break;
			default:
				System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (all) location " + registers.readProgramCounter());
				break;
		}
		registers.writeProgramCounter(registers.readProgramCounter() & FIT_16BIT_REGISTER);
	}
	
	private int convertToUnsignedInt(int value) {
		return value < 0 ? value + 65536 : value;
	}
	
	private int convertToIndex(int coordinateX, int coordinateY) {
		return (coordinateX % getScreen().width()) + ((coordinateY % getScreen().width()) * getScreen().width());
	}
	
	public int readDataRegister(int registerLocation) {
		return registers.readDataRegister(registerLocation);
	}
	
	public int readIndexRegister() {
		return registers.readIndexRegister();
	}
	
	public int readSoundTimer() {
		return soundTimer.currentValue();
	}
	
	public int readDelayTimer() {
		return delayTimer.currentValue();
	}
	
	public int readProgramCounter() {
		return registers.readProgramCounter();
	}
	
	public int readRandomNumber() {
		return randomNumber;
	}
	
	public int readStackTopValue() {
		return stack.isEmpty() ? -1 : stack.peek();
	}
}
