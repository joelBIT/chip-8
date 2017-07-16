package joelbits.emu.cpu;

import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.input.KeyCode;
import joelbits.emu.cpu.instructions.Instructions;
import joelbits.emu.cpu.registers.InstructionRegister;
import joelbits.emu.cpu.registers.ProgramCounter;
import joelbits.emu.cpu.registers.Register;
import joelbits.emu.input.Input;
import joelbits.emu.memory.Memory;
import joelbits.emu.timers.Timer;

/**
 * There are 16 data registers named from V0 to VF. The carry flag (VF) is set
 * to 1 if any screen pixels are flipped from set to unset when a sprite is
 * drawn and set to 0 otherwise. Last 8 bits of each register are used to
 * represent an unsigned byte.
 */
public final class CPU {
	private static final Logger log = LoggerFactory.getLogger(CPU.class);
	private final Memory primaryMemory;
	private final Input<Integer, KeyCode> keyboard;
	private final Stack<Integer> stack;
	private final List<Register<Integer>> dataRegisters;
	private final Register<Integer> instructionRegister = InstructionRegister.getInstance();
	private final Register<Integer> programCounter = ProgramCounter.getInstance();
	private final Register<Integer> indexRegister;
	private final ALU alu;
	private final GPU gpu;
	private final Timer<Integer> delayTimer;
	private final Timer<Integer> soundTimer;
	
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	private int registerLocationX;
	private int registerLocationY;
	private int address;
	private int lowestByte;
	
	public CPU(Stack<Integer> stack, Memory primaryMemory, Input<Integer, KeyCode> keyboard, List<Register<Integer>> dataRegisters, Register<Integer> instructionRegister, Register<Integer> programCounter, Register<Integer> indexRegister, Timer<Integer> delayTimer, Timer<Integer> soundTimer, ALU alu, GPU gpu) {
		this.stack = stack;
		this.primaryMemory = primaryMemory;
		this.keyboard = keyboard;
		this.dataRegisters = dataRegisters;
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
	
	public void loadROM(byte[] ROM, int startLocation) {
		for (int i = 0, location = startLocation; i < ROM.length; i++, location++) {
			primaryMemory.write(location, Byte.toUnsignedInt(ROM[i]));
		}
		resetDataRegisters();
	}
	
	private void resetDataRegisters() {
		for (Register<Integer> register : dataRegisters) {
			register.write(0);
		}
	}
	
	public void executeNextOperation() {
		int instruction = fetchNextInstruction();
		extractInstructionInformation(instruction);
		
		switch(Instructions.getInstruction(String.format("%04X", instruction & 0xFFFF).toUpperCase())) {
			case CLEAR_THE_DISPLAY:
				gpu.clearBuffers();
				programCounter.write(programCounter.read() + 2);
				break;
			case RETURN_FROM_SUBROUTINE:
				programCounter.write(stack.pop() + 2);
				break;
			case JUMP_TO_LOCATION:
				programCounter.write(address);
				break;
			case CALL_SUBROUTINE:
				stack.push(programCounter.read());
				programCounter.write(address);
				break;
			case SKIP_NEXT_INSTRUCTION_IF_VALUES_EQUAL:
				alu.skipNextIfEqual(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case SKIP_NEXT_INSTRUCTION_IF_VALUES_NOT_EQUAL:
				alu.skipNextIfNotEqual(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case SKIP_NEXT_INSTRUCTION_IF_REGISTERS_EQUAL:
				alu.skipNextIfEqual(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case LOAD_BYTE_TO_REGISTER:
				alu.load(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case ADD_BYTE_TO_REGISTER:
				alu.add(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case LOAD_REGISTER_VALUE_TO_REGISTER:
				alu.load(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case BITWISE_OR:
				alu.bitwiseOR(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case BITWISE_AND:
				alu.bitwiseAND(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case BITWISE_XOR:
				alu.bitwiseXOR(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case ADD_REGISTER_VALUE_TO_REGISTER:
				alu.addWithCarry(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read(), FIT_8BIT_REGISTER);
				break;
			case SUBTRACT_REGISTER_VALUE_FROM_REGISTER:
				alu.subtractWithBorrow(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case SHIFT_REGISTER_VALUE_RIGHT:
				alu.rightShiftWithCarry(dataRegisters.get(registerLocationX));
				break;
			case NEGATED_SUBTRACT_REGISTER_VALUE_FROM_REGISTER:
				alu.subtractWithNegatedBorrow(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case SHIFT_REGISTER_VALUE_LEFT:
				alu.leftShiftWithCarry(dataRegisters.get(registerLocationX));
				break;
			case SKIP_NEXT_IF_REGISTERS_NOT_EQUAL:
				alu.skipNextIfNotEqual(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case LOAD_ADDRESS_TO_INDEX_REGISTER:
				alu.load(indexRegister, address);
				break;
			case JUMP_TO_LOCATION_WITH_OFFSET:
				programCounter.write(dataRegisters.get(0x0).read() + address);
				break;
			case SET_RANDOM_BYTE_IN_REGISTER:
				alu.addWithRandom(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case DRAW_SPRITE:
				gpu.drawSprite(dataRegisters, primaryMemory, indexRegister, instruction);
				programCounter.write(programCounter.read() + 2);
				break;
			case SKIP_NEXT_IF_KEY_PRESSED:
				alu.skipNextIfEqual(dataRegisters.get(registerLocationX), keyboard.currentlyPressed());
				break;
			case SKIP_NEXT_IF_KEY_NOT_PRESSED:
				alu.skipNextIfNotEqual(dataRegisters.get(registerLocationX), keyboard.currentlyPressed());
				break;
			case LOAD_REGISTER_WITH_DELAY_TIMER_VALUE:
				alu.load(dataRegisters.get(registerLocationX), delayTimer.currentValue());
				break;
			case WAIT_FOR_KEY_PRESS_AND_STORE_VALUE_IN_REGISTER:
				while (keyboard.currentlyPressed().equals(0)) {
					;
				}
				alu.load(dataRegisters.get(registerLocationX), keyboard.currentlyPressed());
				break;
			case SET_DELAY_TIMER:
				delayTimer.setValue(dataRegisters.get(registerLocationX).read());
				programCounter.write(programCounter.read() + 2);
				break;
			case SET_SOUND_TIMER:
				int value = dataRegisters.get(registerLocationX).read();
				soundTimer.setValue(value == 1 ? 2 : value);
				programCounter.write(programCounter.read() + 2);
				break;
			case ADD_DATA_REGISTER_AND_INDEX_REGISTER:
				alu.addWithCarry(indexRegister, dataRegisters.get(registerLocationX).read(), 0xFFF);
				break;
			case LOAD_SPRITE_LOCATION_TO_REGISTER:
				alu.load(indexRegister, (dataRegisters.get(registerLocationX).read() * 5) & FIT_16BIT_REGISTER);
				break;
			case STORE_BCD_REPRESENTATION_IN_MEMORY:
				writeBcdRepresentationToMemory(registerLocationX);
			 	programCounter.write(programCounter.read() + 2);
			 	break;
			case STORE_DATA_REGISTERS_IN_MEMORY:
				writeDataRegistersToMemory(registerLocationX);
				programCounter.write(programCounter.read() + 2);
				break;
			case LOAD_FROM_MEMORY_TO_DATA_REGISTERS:
				writeMemoryToDataRegisters(registerLocationX);
				programCounter.write(programCounter.read() + 2);
				break;
			default:
				log.warn("Unknown instruction " + Integer.toHexString(instruction & FIT_16BIT_REGISTER) + " at location " + programCounter.read());
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
}
