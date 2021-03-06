package joelbits.emulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.inject.*;
import joelbits.emulator.cache.EmulatorCache;
import joelbits.emulator.config.InterpreterConfig;
import joelbits.emulator.cpu.instructions.InstructionUnit;
import joelbits.emulator.output.Audio;
import joelbits.emulator.settings.GameSettings;
import joelbits.emulator.graphics.GMU;
import joelbits.emulator.memory.MMU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.name.Named;

import javafx.scene.input.KeyCode;
import joelbits.emulator.cpu.ALU;
import joelbits.emulator.cpu.CPU;
import joelbits.emulator.cpu.registers.DataRegister;
import joelbits.emulator.cpu.registers.IndexRegister;
import joelbits.emulator.cpu.registers.ProgramCounter;
import joelbits.emulator.cpu.registers.Register;
import joelbits.emulator.input.Input;
import joelbits.emulator.memory.RAM;
import joelbits.emulator.timers.Timer;
import static joelbits.emulator.utils.Chip8Util.*;
import joelbits.emulator.utils.RandomNumberGenerator;

/**
 * A program is written to memory starting at location 0x200 since the CHIP-8 interpreter occupies
 * most of the preceding memory locations.
 */
public final class Chip8 implements Emulator {
	private static final Logger log = LoggerFactory.getLogger(Chip8.class);
	private final CPU cpu;
	private final MMU mmu;
	
	@Inject
	private Input<Integer, KeyCode> keyboard;
	@Inject
	@Named("sound")
	private Timer<Integer> soundTimer;
	@Inject
	@Named("delay")
	private Timer<Integer> delayTimer;
	@Inject
    private Audio sound;
	@Inject
    private InterpreterConfig config;
	@Inject
    private GameSettings settings;
	@Inject
	private GMU gmu;
	
	public Chip8() {
		EmulatorCache.getInstance().getInjector().injectMembers(this);
		mmu = new MMU(new RAM());
		cpu = createCPU();
	}
	
	private CPU createCPU() {
		List<Register<Integer>> dataRegisters = initializeDataRegisters();
		ALU alu = new ALU(ProgramCounter.getInstance(), dataRegisters.get(REGISTER_VF), new RandomNumberGenerator());
		InstructionUnit instructionUnit = new InstructionUnit(mmu);

		Register<Integer> indexRegister = IndexRegister.getInstance();
		return new CPU(new Stack<>(), mmu, keyboard, dataRegisters, indexRegister, delayTimer, soundTimer, alu, gmu, instructionUnit);
	}

	private List<Register<Integer>> initializeDataRegisters() {
		List<Register<Integer>> dataRegisters = new ArrayList<>();
		for (int i = 0; i <= NUMBER_OF_REGISTERS; i++) {
			dataRegisters.add(i, new DataRegister<>());
			dataRegisters.get(i).write(0);
		}
		return dataRegisters;
	}

	@Override
	public void reset() {
		gmu.clearScreen();
		start();
	}

	@Override
	public void start() {
		cpu.initialize(PROGRAM_SPACE_START,  0x0, 0x0, 0x0, spriteGroups);
		loadProgram();
		if (!settings.isRunning()) {
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new InstructionCycle(), 0, 17, TimeUnit.MILLISECONDS);
			settings.setRunning(true);
		}
	}
	
	private void loadProgram() {
		try {
			cpu.loadProgram(new Program(Files
					.readAllBytes(Paths.get(settings.getGamePath()))), PROGRAM_SPACE_START);
		} catch (IOException e) {
			log.error(e.toString(), e);
		}
	}
	
	class InstructionCycle implements Runnable {
		@Override
		public void run() {
			if (!settings.isPaused()) {
	 			if (delayTimer.getValue() > 0) {
	 				decrementDelayTimer();
	 			}
	 			
	 			if (soundTimer.getValue() > 0) {
                    sound.start();
	 				decrementSoundTimer();
	 				if (soundTimer.getValue() <= 0) {
	 					sound.stop();
	 				}
	 			}
	 			
	 			for (int i = 0; i < settings.getVelocity(); i++) {
	 				if (gmu.isClearFlagActive()) {
	 	 				gmu.clearScreen();
	 	 				gmu.toggleClearFlag();
	 	 			}
	 	 			
	 	 			if (gmu.isDrawFlagActive()) {
	 	 				gmu.drawScreen();
	 	 				gmu.toggleDrawFlag();
	 	 			}
	 				
	 				cpu.executeNextOperation();
	 			}
			}
			return;
		}
		
		private void decrementDelayTimer() {
			delayTimer.setValue(delayTimer.getValue() - 1);
		}
		
		private void decrementSoundTimer() {
			soundTimer.setValue(soundTimer.getValue() - 1);
		}
	}
}
