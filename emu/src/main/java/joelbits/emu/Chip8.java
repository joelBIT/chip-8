package joelbits.emu;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import joelbits.emu.components.TextInputDialogComponent;
import joelbits.emu.cpu.ALU;
import joelbits.emu.cpu.CPU;
import joelbits.emu.cpu.GPU;
import joelbits.emu.cpu.registers.DataRegister;
import joelbits.emu.cpu.registers.IndexRegister;
import joelbits.emu.cpu.registers.InstructionRegister;
import joelbits.emu.cpu.registers.ProgramCounter;
import joelbits.emu.cpu.registers.Register;
import joelbits.emu.flags.ClearFlag;
import joelbits.emu.flags.DrawFlag;
import joelbits.emu.flags.Flag;
import joelbits.emu.input.Input;
import joelbits.emu.input.Keyboard;
import joelbits.emu.memory.BufferFactory;
import joelbits.emu.memory.Memory;
import joelbits.emu.memory.RAM;
import joelbits.emu.output.Screen;
import joelbits.emu.output.Sound;
import joelbits.emu.timers.DelayTimer;
import joelbits.emu.timers.SoundTimer;
import joelbits.emu.timers.Timer;
import joelbits.emu.utils.ROMFileChooser;
import joelbits.emu.utils.Chip8Util;
import joelbits.emu.utils.RandomNumberGenerator;

/**
 * A ROM is written to memory starting at location 0x200 since the CHIP-8 interpreter occupies most of the preceding memory locations.
 * 
 */
public final class Chip8 extends Application {
	private GraphicsContext graphicsContext;
	private Stage stage;
	private CPU cpu;
	private GPU gpu;
	private final Timer<Integer> delayTimer = new DelayTimer<Integer>();
	private final Timer<Integer> soundTimer = new SoundTimer<Integer>();
	private final Flag drawFlag = new DrawFlag();
	private final Flag clearFlag = new ClearFlag();
	private final Input<Integer, KeyCode> keyboard = new Keyboard();
	private final Sound sound = new Sound();
	private final BorderPane root = new BorderPane();
	private final GameSettings settings = new GameSettings();
	private final Chip8Util chip8Util = new Chip8Util();
	private final TextInputDialogComponent velocityDialog = new TextInputDialogComponent(settings);
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		stage.setTitle("Chip-8 interpreter");
		
		Canvas canvas = new Canvas(896, 448);
		graphicsContext = canvas.getGraphicsContext2D();
		graphicsContext.setFill(Color.WHITE);
		
		gpu = createGPU(graphicsContext, drawFlag, clearFlag);
		cpu = createCPU(gpu, keyboard, delayTimer, soundTimer);
		
		root.setStyle("-fx-background: black;");
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setOnCloseRequest(event -> chip8Util.terminateApplication());
		
		scene.setOnKeyPressed(event -> keyboard.press(event.getCode()));
		scene.setOnKeyReleased(event -> keyboard.releasePressed());
		
		root.setTop(createMenuBar());
		root.setBottom(canvas);
		
		stage.show();
	}
	
	private GPU createGPU(GraphicsContext graphicsContext, Flag drawFlag, Flag clearFlag) {
		Memory displayBuffer = BufferFactory.createDisplayBuffer(chip8Util.getScreenWidth(), chip8Util.getScreenHeight());
		Memory dirtyBuffer = BufferFactory.createDirtyBuffer();
		return new GPU(displayBuffer, dirtyBuffer, new Screen<Integer>(chip8Util.getScreenWidth(), chip8Util.getScreenHeight(), chip8Util.getPixelSize()), graphicsContext, drawFlag, clearFlag);
	}
	
	private CPU createCPU(GPU gpu, Input<Integer, KeyCode> keyboard, Timer<Integer> delayTimer, Timer<Integer> soundTimer) {
		List<Register<Integer>> dataRegisters = initializeDataRegisters();
		Register<Integer> programCounter = ProgramCounter.getInstance();
		ALU alu = new ALU(programCounter, dataRegisters.get(0xF), new RandomNumberGenerator());
		
		Register<Integer> instructionRegister = InstructionRegister.getInstance();
		Register<Integer> indexRegister = IndexRegister.getInstance();
		return new CPU(new Stack<Integer>(), new RAM(4096), keyboard, dataRegisters, instructionRegister, programCounter, indexRegister, delayTimer, soundTimer, alu, gpu);
	}

	private List<Register<Integer>> initializeDataRegisters() {
		List<Register<Integer>> dataRegisters = new ArrayList<>();
		for (int i = 0; i <= 0xF; i++) {
			dataRegisters.add(i, new DataRegister<Integer>());
			dataRegisters.get(i).write(0);
		}
		return dataRegisters;
	}
	
	private MenuBar createMenuBar() {
		Menu interpreter = createInterpreterMenu();
		Menu options = createOptionsMenu();
		Menu game = createGameMenu();
		
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(interpreter, options, game);
		return menuBar;
	}
	
	private Menu createInterpreterMenu() {
		Menu interpreter = new Menu("Interpreter");
		interpreter.setOnShowing(event -> settings.setPaused(true));
		interpreter.setOnHidden(event -> settings.setPaused(false));
		
		MenuItem open = new MenuItem("Open");
		open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		open.setOnAction(event -> {
			settings.setPaused(true);
			openROM();
			settings.setPaused(false);
		});
		
		MenuItem exit = new MenuItem("Exit");
		exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
		exit.setOnAction(event -> chip8Util.terminateApplication());
		
		SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
		interpreter.getItems().addAll(open, separatorMenuItem, exit);
		return interpreter;
	}
	
	private void openROM() {
		File file = new ROMFileChooser().showOpenDialog(stage);
		if (file != null) {
			settings.setGamePath(file.toURI());
			resetGame();
		}
	}
	
	private Menu createOptionsMenu() {
		Menu options = new Menu("Options");
		options.setOnShowing(event -> settings.setPaused(true));
		options.setOnHidden(event -> settings.setPaused(false));
		
		CheckMenuItem muteSound = new CheckMenuItem("Mute Sound");
		muteSound.setAccelerator(new KeyCodeCombination(KeyCode.F4));
		muteSound.setOnAction(event -> { toggleMute(muteSound); });
		
		MenuItem velocity = new MenuItem("Change velocity");
		velocity.setAccelerator(new KeyCodeCombination(KeyCode.F5));
		velocity.setOnAction(event -> {
			settings.setPaused(true);
			velocityDialog.changeGameVelocity();
			settings.setPaused(false);
		});
		
		SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
		options.getItems().addAll(muteSound, separatorMenuItem, velocity);
		return options;
	}
	
	private void toggleMute(CheckMenuItem muteSound) {
		if (muteSound.isSelected()) {
			sound.mute();
		} else {
			sound.unmute();
		}
	}
	
	private Menu createGameMenu() {
		Menu game = new Menu("Game");
		game.setOnShowing(event -> settings.setPaused(true));
		game.setOnHidden(event -> settings.setPaused(false));
		
		CheckMenuItem pause = new CheckMenuItem("Pause");
		pause.setAccelerator(new KeyCodeCombination(KeyCode.F2));
		pause.setOnAction(event -> settings.setPaused(pause.isSelected()));
		
		MenuItem reset = new MenuItem("Reset");
		reset.setAccelerator(new KeyCodeCombination(KeyCode.F3));
		reset.setOnAction(event -> resetGame());
		
		game.getItems().addAll(pause, reset);
		return game;
	}
	
	private void resetGame() {
		gpu.clearScreen();
		startGame(settings.getGamePath());
	}
	
	private void startGame(URI gamePath) {
		cpu.initialize(0x200, 0x0, 0x0, 0x0, 0x0, chip8Util.getFontSet());
		loadGame();
		if (!settings.isRunning()) {
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new InstructionCycle(), 0, 17, TimeUnit.MILLISECONDS);
			settings.setRunning(true);
		}
	}
	
	private void loadGame() {
		try {
			cpu.loadROM(chip8Util.readROM(settings.getGamePath()), 0x200);
		} catch (IOException e) {
			e.printStackTrace();
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
