package joelbits.emu.cpu.instructions;

public enum Instructions {
	CLEAR_THE_DISPLAY("00E0"),
	RETURN_FROM_SUBROUTINE("00EE"),
	JUMP_TO_LOCATION("1xxx"),
	CALL_SUBROUTINE("2xxx"),
	SKIP_NEXT_INSTRUCTION_IF_VALUES_EQUAL("3xxx"),
	SKIP_NEXT_INSTRUCTION_IF_VALUES_NOT_EQUAL("4xxx"),
	SKIP_NEXT_INSTRUCTION_IF_REGISTERS_EQUAL("5xx0"),
	LOAD_BYTE_TO_REGISTER("6xxx"),
	ADD_BYTE_TO_REGISTER("7xxx"),
	LOAD_REGISTER_VALUE_TO_REGISTER("8xx0"),
	BITWISE_OR("8xx1"),
	BITWISE_AND("8xx2"),
	BITWISE_XOR("8xx3"),
	ADD_REGISTER_VALUE_TO_REGISTER("8xx4"),
	SUBTRACT_REGISTER_VALUE_FROM_REGISTER("8xx5"),
	SHIFT_REGISTER_VALUE_RIGHT("8xx6"),
	NEGATED_SUBTRACT_REGISTER_VALUE_FROM_REGISTER("8xx7"),
	SHIFT_REGISTER_VALUE_LEFT("8xxE"),
	SKIP_NEXT_IF_REGISTERS_NOT_EQUAL("9xxx"),
	LOAD_ADDRESS_TO_INDEX_REGISTER("Axxx"),
	JUMP_TO_LOCATION_WITH_OFFSET("Bxxx"),
	SET_RANDOM_BYTE_IN_REGISTER("Cxxx"),
	DRAW_SPRITE("Dxxx"),
	SKIP_NEXT_IF_KEY_PRESSED("Ex9E"),
	SKIP_NEXT_IF_KEY_NOT_PRESSED("ExA1"),
	LOAD_REGISTER_WITH_DELAY_TIMER_VALUE("Fx07"),
	WAIT_FOR_KEY_PRESS_AND_STORE_VALUE_IN_REGISTER("Fx0A"),
	SET_DELAY_TIMER("Fx15"),
	SET_SOUND_TIMER("Fx18"),
	ADD_DATA_REGISTER_AND_INDEX_REGISTER("Fx1E"),
	LOAD_SPRITE_LOCATION_TO_REGISTER("Fx29"),
	STORE_BCD_REPRESENTATION_IN_MEMORY("Fx33"),
	STORE_DATA_REGISTERS_IN_MEMORY("Fx55"),
	LOAD_FROM_MEMORY_TO_DATA_REGISTERS("Fx65");
	
	private final String opcode;
	
	Instructions(String opcode) {
		this.opcode = opcode;
	}
	
	public static Instructions getInstruction(String opcode) {
		for (Instructions instruction : Instructions.values()) {
			if (isEqual(instruction.opcode, opcode)) {
				return instruction;
			}
		}
		throw new IllegalArgumentException("Not a valid opcode");
	}
	
	private static boolean isEqual(String instruction, String opcode) {
		if (instruction.length() != opcode.length()) {
			throw new IllegalArgumentException("The opcodes differ in length");
		}
		for (int i = 0; i < opcode.length(); i++) {
			char instructionChar = instruction.charAt(i);
			if (instructionChar != 'x' && instructionChar != opcode.charAt(i)) {
				return false;
			}
		}
		return true;
	}
}
