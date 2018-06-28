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
import joelbits.emulator.modules.InterpreterModule;
import joelbits.emulator.settings.GameSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.input.KeyCode;
import joelbits.emulator.cpu.ALU;
import joelbits.emulator.cpu.CPU;
import joelbits.emulator.cpu.GPU;
import joelbits.emulator.cpu.registers.DataRegister;
import joelbits.emulator.cpu.registers.IndexRegister;
import joelbits.emulator.cpu.registers.InstructionRegister;
import joelbits.emulator.cpu.registers.ProgramCounter;
import joelbits.emulator.cpu.registers.Register;
import joelbits.emulator.flags.Flag;
import joelbits.emulator.input.Input;
import joelbits.emulator.memory.BufferFactory;
import joelbits.emulator.memory.Memory;
import joelbits.emulator.memory.RAM;
import joelbits.emulator.output.Screen;
import joelbits.emulator.output.Sound;
import joelbits.emulator.timers.Timer;
import joelbits.emulator.utils.Chip8Util;
import joelbits.emulator.utils.RandomNumberGenerator;

/**
 * A ROM is written to memory starting at location 0x200 since the CHIP-8 interpreter occupies
 * most of the preceding memory locations.
 */
public final class Chip8 {
	private static final Logger log = LoggerFactory.getLogger(Chip8.class);
	private final CPU cpu;
	private final GPU gpu;
	private final GameSettings settings;
	
	@Inject
	private Input<Integer, KeyCode> keyboard;
	@Inject
	@Named("clear")
	private Flag clearFlag;
	@Inject
	@Named("draw")
	private Flag drawFlag;
	@Inject
	@Named("sound")
	private Timer<Integer> soundTimer;
	@Inject
	@Named("delay")
	private Timer<Integer> delayTimer;
	@Inject
	private Sound sound;
	
	public Chip8(GameSettings settings, GraphicsContext graphicsContext) {
		Guice.createInjector(new InterpreterModule()).injectMembers(this);
		this.settings = settings;
		
		gpu = createGPU(graphicsContext, drawFlag, clearFlag);
		cpu = createCPU(gpu, delayTimer, soundTimer);
	}
	
	private GPU createGPU(GraphicsContext graphicsContext, Flag drawFlag, Flag clearFlag) {
		InterpreterConfig config = new InterpreterConfig();
		Memory displayBuffer = BufferFactory.createDisplayBuffer(config.screenWidth(), config.screenHeight());
		Memory dirtyBuffer = BufferFactory.createDirtyBuffer();
		Screen<Integer> screen = new Screen<Integer>(config.screenWidth(), config.screenHeight(), config.pixelSize());
		
		return new GPU(displayBuffer, dirtyBuffer, screen, graphicsContext, drawFlag, clearFlag);
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
	
	public Input<Integer, KeyCode> keyboard() {
		return keyboard;
	}
	
	public void resetGame() {
		gpu.clearScreen();
		startGame(settings.getGamePath());
	}
	
	private void startGame(URI gamePath) {
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
	
	public void toggleMute(CheckMenuItem muteSound) {
		if (muteSound.isSelected()) {
			sound.mute();
		} else {
			sound.unmute();
		}
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
	 				if (clearFlag.isActive()) {
	 	 				gpu.clearScreen();
	 	 				clearFlag.toggle();
	 	 			}
	 	 			
	 	 			if (drawFlag.isActive()) {
	 	 				gpu.drawScreen();
	 	 				drawFlag.toggle();
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
