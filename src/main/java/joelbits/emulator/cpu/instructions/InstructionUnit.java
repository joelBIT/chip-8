package joelbits.emulator.cpu.instructions;

import joelbits.emulator.memory.MMU;
import static joelbits.emulator.utils.Chip8Util.FIT_8BIT_REGISTER;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InstructionUnit {
    @Getter(AccessLevel.NONE) private final MMU mmu;
    private int registerLocationX;
    private int registerLocationY;
    private int address;
    private int lowestByte;

    public int fetchNextInstruction(int programCounter) {
        int instruction = mmu.readPrimaryMemory(programCounter) << 8 | mmu.readPrimaryMemory(programCounter + 1);
        extractInstructionInformation(instruction);

        return instruction;
    }

    private void extractInstructionInformation(int instruction) {
        registerLocationX = (instruction & 0x0F00) >> 8;
        registerLocationY = (instruction & 0x00F0) >> 4;
        address = instruction & 0x0FFF;
        lowestByte = instruction & FIT_8BIT_REGISTER;
    }
}
