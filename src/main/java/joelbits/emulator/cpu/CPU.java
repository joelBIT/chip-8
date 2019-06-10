package joelbits.emulator.cpu;

import java.util.List;
import java.util.Stack;

import joelbits.emulator.Program;
import joelbits.emulator.units.GMU;
import joelbits.emulator.units.MMU;
import joelbits.emulator.utils.Chip8Util;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.input.KeyCode;
import joelbits.emulator.cpu.instructions.Instructions;
import joelbits.emulator.cpu.registers.Register;
import joelbits.emulator.input.Input;
import joelbits.emulator.timers.Timer;

/**
 * There are 16 data registers named from V0 to VF. The carry flag (VF) is set
 * to 1 if any screen pixels are flipped from set to unset when a sprite is
 * drawn and set to 0 otherwise. Last 8 bits of each register are used to
 * represent an unsigned byte.
 */
@RequiredArgsConstructor
public final class CPU {
	private static final Logger log = LoggerFactory.getLogger(CPU.class);
	private final Stack<Integer> stack;
	private final MMU mmu;
	private final Input<Integer, KeyCode> keyboard;
	private final List<Register<Integer>> dataRegisters;
	private final Register<Integer> indexRegister;
	private final Timer<Integer> delayTimer;
	private final Timer<Integer> soundTimer;
	private final ALU alu;
	private final GMU gmu;
	private int registerLocationX;
	private int registerLocationY;
	private int address;
	private int lowestByte;

	// Skapa alla instruktioner som egna objekt vid uppstart av applikationen med hjälp av builders för varje. Köra dessa sedan genom en generisk execute(Instruction instruciton) metod/klass?

	public void initialize(int address, int index, int delayTime, int soundTime, int[] data) {
		alu.setProgramCounter(address);
		delayTimer.setValue(delayTime);
		soundTimer.setValue(soundTime);
		indexRegister.write(index);
		
		gmu.clearBuffers();
		mmu.clearPrimaryMemory();
		mmu.writePrimaryMemory(data);
	}
	
	public void loadProgram(Program program, int startLocation) {
		for (int i = 0, location = startLocation; i < program.size(); i++, location++) {
			mmu.writePrimaryMemory(location, Byte.toUnsignedInt(program.data(i)));
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
		
		switch(Instructions.getInstruction(String.format("%04X", instruction & Chip8Util.FIT_16BIT_REGISTER).toUpperCase())) {
			case CLEAR_THE_DISPLAY:
				gmu.clearBuffers();
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			case RETURN_FROM_SUBROUTINE:
				alu.setProgramCounter(stack.pop() + 2);
				break;
			case JUMP_TO_LOCATION:
				alu.setProgramCounter(address);
				break;
			case CALL_SUBROUTINE:
				stack.push(alu.programCounter());
				alu.setProgramCounter(address);
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
				alu.addWithCarry(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read(), Chip8Util.FIT_8BIT_REGISTER);
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
				alu.setProgramCounter(dataRegisters.get(0x0).read() + address);
				break;
			case SET_RANDOM_BYTE_IN_REGISTER:
				alu.addWithRandom(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case DRAW_SPRITE:
				gmu.drawSprite(dataRegisters, mmu.primaryMemory(), indexRegister, instruction);
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			case SKIP_NEXT_IF_KEY_PRESSED:
				alu.skipNextIfEqual(dataRegisters.get(registerLocationX), keyboard.currentlyPressed());
				break;
			case SKIP_NEXT_IF_KEY_NOT_PRESSED:
				alu.skipNextIfNotEqual(dataRegisters.get(registerLocationX), keyboard.currentlyPressed());
				break;
			case LOAD_REGISTER_WITH_DELAY_TIMER_VALUE:
				alu.load(dataRegisters.get(registerLocationX), delayTimer.getValue());
				break;
			case WAIT_FOR_KEY_PRESS_AND_STORE_VALUE_IN_REGISTER:
				while (keyboard.currentlyPressed().equals(0)) {
					;
				}
				alu.load(dataRegisters.get(registerLocationX), keyboard.currentlyPressed());
				break;
			case SET_DELAY_TIMER:
				delayTimer.setValue(dataRegisters.get(registerLocationX).read());
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			case SET_SOUND_TIMER:
				int value = dataRegisters.get(registerLocationX).read();
				soundTimer.setValue(value == 1 ? 2 : value);
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			case ADD_DATA_REGISTER_AND_INDEX_REGISTER:
				alu.addWithCarry(indexRegister, dataRegisters.get(registerLocationX).read(), 0xFFF);
				break;
			case LOAD_SPRITE_LOCATION_TO_REGISTER:
				alu.load(indexRegister, (dataRegisters.get(registerLocationX).read() * 5) & Chip8Util.FIT_16BIT_REGISTER);
				break;
			case STORE_BCD_REPRESENTATION_IN_MEMORY:
				writeBcdRepresentationToMemory(registerLocationX);
			 	alu.setProgramCounter(alu.programCounter() + 2);
			 	break;
			case STORE_DATA_REGISTERS_IN_MEMORY:
				writeDataRegistersToMemory(registerLocationX);
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			case LOAD_FROM_MEMORY_TO_DATA_REGISTERS:
				writeMemoryToDataRegisters(registerLocationX);
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			default:
				log.warn("Unknown instruction " + Integer.toHexString(instruction & Chip8Util.FIT_16BIT_REGISTER) + " at location " + alu.programCounter());
				break;
		}
	}
	
	private int fetchNextInstruction() {
		return mmu.readPrimaryMemory(alu.programCounter()) << 8 | mmu.readPrimaryMemory(alu.programCounter()+1);
	}
	
	private void extractInstructionInformation(int instruction) {
		registerLocationX = (instruction & 0x0F00) >> 8;
		registerLocationY = (instruction & 0x00F0) >> 4;
		address = instruction & 0x0FFF;
		lowestByte = instruction & Chip8Util.FIT_8BIT_REGISTER;
	}
	
	private void writeBcdRepresentationToMemory(int registerLocation) {
		mmu.writePrimaryMemory(indexRegister.read(), dataRegisters.get(registerLocation).read() / 100);
 		mmu.writePrimaryMemory(indexRegister.read() + 1, (dataRegisters.get(registerLocation).read() % 100) / 10);
 		mmu.writePrimaryMemory(indexRegister.read() + 2, dataRegisters.get(registerLocation).read() % 10);
	}
	
	private void writeDataRegistersToMemory(int registerBound) {
		for (int i = 0; i <= registerBound; i++) {
			mmu.writePrimaryMemory(indexRegister.read() + i, dataRegisters.get(i).read());
		}
	}
	
	private void writeMemoryToDataRegisters(int registerBound) {
		for (int i = 0; i <= registerBound; i++) {
			dataRegisters.get(i).write(mmu.readPrimaryMemory(indexRegister.read() + i));
		}
	}
}
