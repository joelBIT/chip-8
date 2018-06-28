package joelbits.emulator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import joelbits.emulator.config.InterpreterConfig;
import joelbits.emulator.modules.ModuleFactory;
import joelbits.emulator.output.Audio;
import joelbits.emulator.settings.GameSettings;
import joelbits.emulator.units.GMU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import javafx.scene.input.KeyCode;
import joelbits.emulator.cpu.ALU;
import joelbits.emulator.cpu.CPU;
import joelbits.emulator.units.GPU;
import joelbits.emulator.cpu.registers.DataRegister;
import joelbits.emulator.cpu.registers.IndexRegister;
import joelbits.emulator.cpu.registers.InstructionRegister;
import joelbits.emulator.cpu.registers.ProgramCounter;
import joelbits.emulator.cpu.registers.Register;
import joelbits.emulator.input.Input;
import joelbits.emulator.memory.RAM;
import joelbits.emulator.timers.Timer;
import joelbits.emulator.utils.Chip8Util;
import joelbits.emulator.utils.RandomNumberGenerator;

/**
 * A ROM is written to memory starting at location 0x200 since the CHIP-8 interpreter occupies
 * most of the preceding memory locations.
 */
public final class Chip8 implements Emulator {
	private static final Logger log = LoggerFactory.getLogger(Chip8.class);
	private final CPU cpu;
	private final GMU gmu;
	
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
	
	public Chip8(GMU gmu) {
		Guice.createInjector(ModuleFactory.interpreterModule(), ModuleFactory.soundModule(), ModuleFactory
                .settingsModule(), ModuleFactory.keyboardModule())
                .injectMembers(this);
		this.gmu = gmu;
		cpu = createCPU(gmu.gpu(), delayTimer, soundTimer);
	}
	
	private CPU createCPU(GPU gpu, Timer<Integer> delayTimer, Timer<Integer> soundTimer) {
		List<Register<Integer>> dataRegisters = initializeDataRegisters();
		Register<Integer> programCounter = ProgramCounter.getInstance();
		ALU alu = new ALU(programCounter, dataRegisters.get(0xF), new RandomNumberGenerator());
		
		Register<Integer> instructionRegister = InstructionRegister.getInstance();
		Register<Integer> indexRegister = IndexRegister.getInstance();
		return new CPU(new Stack<Integer>(), new RAM(), keyboard, dataRegisters, instructionRegister, programCounter, indexRegister, delayTimer, soundTimer, alu, gpu);
	}

	private List<Register<Integer>> initializeDataRegisters() {
		List<Register<Integer>> dataRegisters = new ArrayList<>();
		for (int i = 0; i <= 0xF; i++) {
			dataRegisters.add(i, new DataRegister<Integer>());
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
		cpu.initialize(0x200, 0x0, 0x0, 0x0, 0x0, Chip8Util.fontset);
		loadGame();
		if (!settings.isRunning()) {
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new InstructionCycle(), 0, 17, TimeUnit.MILLISECONDS);
			settings.setRunning(true);
		}
	}
	
	private void loadGame() {
		try {
			cpu.loadROM(readROM(settings.getGamePath()), 0x200);
		} catch (IOException e) {
			log.error(e.toString(), e);
		}
	}

	private byte[] readROM(URI gamePath) throws IOException {
		return Files.readAllBytes(Paths.get(gamePath));
	}
	
	class InstructionCycle implements Runnable {
		@Override
		public void run() {
			if (!settings.isPaused()) {
	 			if (delayTimer.currentValue() > 0) {
	 				decrementDelayTimer();
	 			}
	 			
	 			if (soundTimer.currentValue() > 0) {
                    sound.start();
	 				decrementSoundTimer();
	 				if (soundTimer.currentValue() <= 0) {
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
			delayTimer.setValue(delayTimer.currentValue() - 1);
		}
		
		private void decrementSoundTimer() {
			soundTimer.setValue(soundTimer.currentValue() - 1);
		}
	}
}
