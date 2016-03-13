package joelbits.emu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import joelbits.emu.cpu.CPU;

/**
 * Chip-8 emulator.
 * 
 * Last 8 or 16 bits of each int are used to represent an unsigned byte or an unsigned short respectively.
 * 
 * @author rollnyj
 * 
 */
public class Chip8 {
	private final CPU cpu = new CPU();
	
	public static void main(String[] args) {
		Chip8 chip8 = new Chip8();
		
		chip8.initialize();
		chip8.loadGame("pong");
		
		for (;;) {
			chip8.getCPU().nextInstructionCycle();
		}
	}
	
	public void initialize() {
		getCPU().setProgramCounter(0x200);
		getCPU().setInstructionRegister(0x0);
		getCPU().setIndexRegister(0x0);
		getCPU().setDelayTimer(0x0);
		getCPU().setSoundTimer(0x0);
		getCPU().getExpansionBus().getDisplay().clearDisplayBuffer();
		getCPU().getMemoryBus().getPrimaryMemory().clearMemory();
	}
	
	private CPU getCPU() {
		return cpu;
	}
	
	private void loadGame(String game) {
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(game));
			for (int i = 0, location = getCPU().getProgramCounter(); i < bytes.length; i++, location++) {
				getCPU().getMemoryBus().getPrimaryMemory().writeToMemory(Byte.toUnsignedInt(bytes[i]), location);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
