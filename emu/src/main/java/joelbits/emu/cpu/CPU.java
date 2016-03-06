package joelbits.emu.cpu;

/**
 * There are 16 data registers named from V0 to VF. The carry flag (VF) is set to 1 if any screen pixels are flipped from set 
 * to unset when a sprite is drawn and set to 0 otherwise. Last 8 bits of each register are used to represent an unsigned byte.
 * 
 * @author rollnyj
 *
 */
public class CPU {
	private MemoryBus memoryBus = new MemoryBus();
	private ALU alu = new ALU();
	private short[] registers = new short[16];
	private int instructionRegister;
	private int programCounter;
	
	public void setProgramCounter(int address) {
		this.programCounter = address;
	}
	
	public void setInstructionRegister(int address) {
		this.instructionRegister = address;
	}
	
	public MemoryBus getMemoryBus() {
		return memoryBus;
	}
	
	class CU {
		
	}
}
