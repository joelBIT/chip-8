package joelbits.emulator.cpu;

import java.util.List;
import java.util.Stack;

import joelbits.emulator.Program;
import joelbits.emulator.cpu.instructions.InstructionUnit;
import joelbits.emulator.graphics.GMU;
import joelbits.emulator.memory.MMU;
import static joelbits.emulator.utils.Chip8Util.*;
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
	private final InstructionUnit instructionUnit;

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
		int instruction = instructionUnit.fetchNextInstruction(alu.programCounter());
		
		switch(Instructions.getInstruction(String.format("%04X", instruction & FIT_16BIT_REGISTER).toUpperCase())) {
			case CLEAR_THE_DISPLAY:
				gmu.clearBuffers();
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			case RETURN_FROM_SUBROUTINE:
				alu.setProgramCounter(stack.pop() + 2);
				break;
			case JUMP_TO_LOCATION:
				alu.setProgramCounter(instructionUnit.getAddress());
				break;
			case CALL_SUBROUTINE:
				stack.push(alu.programCounter());
				alu.setProgramCounter(instructionUnit.getAddress());
				break;
			case SKIP_NEXT_INSTRUCTION_IF_VALUES_EQUAL:
				alu.skipNextIfEqual(dataRegisters.get(instructionUnit.getRegisterLocationX()), instructionUnit.getLowestByte());
				break;
			case SKIP_NEXT_INSTRUCTION_IF_VALUES_NOT_EQUAL:
				alu.skipNextIfNotEqual(dataRegisters.get(instructionUnit.getRegisterLocationX()), instructionUnit.getLowestByte());
				break;
			case SKIP_NEXT_INSTRUCTION_IF_REGISTERS_EQUAL:
				alu.skipNextIfEqual(dataRegisters.get(instructionUnit.getRegisterLocationX()), dataRegisters.get(instructionUnit.getRegisterLocationY()).read());
				break;
			case LOAD_BYTE_TO_REGISTER:
				alu.load(dataRegisters.get(instructionUnit.getRegisterLocationX()), instructionUnit.getLowestByte());
				break;
			case ADD_BYTE_TO_REGISTER:
				alu.add(dataRegisters.get(instructionUnit.getRegisterLocationX()), instructionUnit.getLowestByte());
				break;
			case LOAD_REGISTER_VALUE_TO_REGISTER:
				alu.load(dataRegisters.get(instructionUnit.getRegisterLocationX()), dataRegisters.get(instructionUnit.getRegisterLocationY()).read());
				break;
			case BITWISE_OR:
				alu.bitwiseOR(dataRegisters.get(instructionUnit.getRegisterLocationX()), dataRegisters.get(instructionUnit.getRegisterLocationY()).read());
				break;
			case BITWISE_AND:
				alu.bitwiseAND(dataRegisters.get(instructionUnit.getRegisterLocationX()), dataRegisters.get(instructionUnit.getRegisterLocationY()).read());
				break;
			case BITWISE_XOR:
				alu.bitwiseXOR(dataRegisters.get(instructionUnit.getRegisterLocationX()), dataRegisters.get(instructionUnit.getRegisterLocationY()).read());
				break;
			case ADD_REGISTER_VALUE_TO_REGISTER:
				alu.addWithCarry(dataRegisters.get(instructionUnit.getRegisterLocationX()), dataRegisters.get(instructionUnit.getRegisterLocationY()).read(), FIT_8BIT_REGISTER);
				break;
			case SUBTRACT_REGISTER_VALUE_FROM_REGISTER:
				alu.subtractWithBorrow(dataRegisters.get(instructionUnit.getRegisterLocationX()), dataRegisters.get(instructionUnit.getRegisterLocationY()).read());
				break;
			case SHIFT_REGISTER_VALUE_RIGHT:
				alu.rightShiftWithCarry(dataRegisters.get(instructionUnit.getRegisterLocationX()));
				break;
			case NEGATED_SUBTRACT_REGISTER_VALUE_FROM_REGISTER:
				alu.subtractWithNegatedBorrow(dataRegisters.get(instructionUnit.getRegisterLocationX()), dataRegisters.get(instructionUnit.getRegisterLocationY()).read());
				break;
			case SHIFT_REGISTER_VALUE_LEFT:
				alu.leftShiftWithCarry(dataRegisters.get(instructionUnit.getRegisterLocationX()));
				break;
			case SKIP_NEXT_IF_REGISTERS_NOT_EQUAL:
				alu.skipNextIfNotEqual(dataRegisters.get(instructionUnit.getRegisterLocationX()), dataRegisters.get(instructionUnit.getRegisterLocationY()).read());
				break;
			case LOAD_ADDRESS_TO_INDEX_REGISTER:
				alu.load(indexRegister, instructionUnit.getAddress());
				break;
			case JUMP_TO_LOCATION_WITH_OFFSET:
				alu.setProgramCounter(dataRegisters.get(0x0).read() + instructionUnit.getAddress());
				break;
			case SET_RANDOM_BYTE_IN_REGISTER:
				alu.addWithRandom(dataRegisters.get(instructionUnit.getRegisterLocationX()), instructionUnit.getLowestByte());
				break;
			case DRAW_SPRITE:
				gmu.drawSprite(dataRegisters, mmu.primaryMemory(), indexRegister, instruction);
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			case SKIP_NEXT_IF_KEY_PRESSED:
				alu.skipNextIfEqual(dataRegisters.get(instructionUnit.getRegisterLocationX()), keyboard.currentlyPressed());
				break;
			case SKIP_NEXT_IF_KEY_NOT_PRESSED:
				alu.skipNextIfNotEqual(dataRegisters.get(instructionUnit.getRegisterLocationX()), keyboard.currentlyPressed());
				break;
			case LOAD_REGISTER_WITH_DELAY_TIMER_VALUE:
				alu.load(dataRegisters.get(instructionUnit.getRegisterLocationX()), delayTimer.getValue());
				break;
			case WAIT_FOR_KEY_PRESS_AND_STORE_VALUE_IN_REGISTER:
				while (keyboard.currentlyPressed().equals(0)) {
					;
				}
				alu.load(dataRegisters.get(instructionUnit.getRegisterLocationX()), keyboard.currentlyPressed());
				break;
			case SET_DELAY_TIMER:
				delayTimer.setValue(dataRegisters.get(instructionUnit.getRegisterLocationX()).read());
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			case SET_SOUND_TIMER:
				int value = dataRegisters.get(instructionUnit.getRegisterLocationX()).read();
				soundTimer.setValue(value == 1 ? 2 : value);
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			case ADD_DATA_REGISTER_AND_INDEX_REGISTER:
				alu.addWithCarry(indexRegister, dataRegisters.get(instructionUnit.getRegisterLocationX()).read(), 0xFFF);
				break;
			case LOAD_SPRITE_LOCATION_TO_REGISTER:
				alu.load(indexRegister, (dataRegisters.get(instructionUnit.getRegisterLocationX()).read() * 5) & FIT_16BIT_REGISTER);
				break;
			case STORE_BCD_REPRESENTATION_IN_MEMORY:
				writeBcdRepresentationToMemory(instructionUnit.getRegisterLocationX());
			 	alu.setProgramCounter(alu.programCounter() + 2);
			 	break;
			case STORE_DATA_REGISTERS_IN_MEMORY:
				writeDataRegistersToMemory(instructionUnit.getRegisterLocationX());
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			case LOAD_FROM_MEMORY_TO_DATA_REGISTERS:
				writeMemoryToDataRegisters(instructionUnit.getRegisterLocationX());
				alu.setProgramCounter(alu.programCounter() + 2);
				break;
			default:
				log.warn("Unknown instruction " + Integer.toHexString(instruction & FIT_16BIT_REGISTER) + " at location " + alu.programCounter());
				break;
		}
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
