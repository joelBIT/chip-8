package joelbits.emu;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
import joelbits.emu.output.Beep;
import joelbits.emu.output.Screen;
import joelbits.emu.output.Sound;
import joelbits.emu.timers.DelayTimer;
import joelbits.emu.timers.SoundTimer;
import joelbits.emu.timers.Timer;

/**
 * A ROM is written to memory starting at location 0x200 since the CHIP-8 interpreter occupies most of the preceding memory locations.
 * 
 */
public final class Chip8 extends Application {
	private GraphicsContext graphicsContext;
	private Stage stage;
	private CPU cpu;
	private GPU gpu;
	private URI gamePath;
	private final Timer<Integer> delayTimer = new DelayTimer<Integer>();
	private final Timer<Integer> soundTimer = new SoundTimer<Integer>();
	private final Flag drawFlag = new DrawFlag();
	private final Flag clearFlag = new ClearFlag();
	private final Input<Integer, KeyCode> keyboard = new Keyboard();
	private final Sound sound = new Beep();
	private final BorderPane root = new BorderPane();
	private final FileChooser fileChooser = new FileChooser();
	private boolean running;
	private boolean paused;
	private int GAME_VELOCITY = 10;
	private final int SCREEN_WIDTH = 64;
	private final int SCREEN_HEIGHT = 32;
	private final int PIXEL_SIZE = 14;
	private final int fontset[] =
		{ 
		  0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
		  0x20, 0x60, 0x20, 0x20, 0x70, // 1
		  0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
		  0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
		  0x90, 0x90, 0xF0, 0x10, 0x10, // 4
		  0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
		  0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
		  0xF0, 0x10, 0x20, 0x40, 0x40, // 7
		  0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
		  0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
		  0xF0, 0x90, 0xF0, 0x90, 0x90, // A
		  0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
		  0xF0, 0x80, 0x80, 0x80, 0xF0, // C
		  0xE0, 0x90, 0x90, 0x90, 0xE0, // D
		  0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
		  0xF0, 0x80, 0xF0, 0x80, 0x80  // F
	};
	
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
		
		initializeUnits();
		
		root.setStyle("-fx-background: black;");
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setOnCloseRequest(event -> terminateApplication());
		
		scene.setOnKeyPressed(event -> keyboard.press(event.getCode()));
		scene.setOnKeyReleased(event -> keyboard.releasePressed());
		
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("rom", "*.rom"), new FileChooser.ExtensionFilter("ch8", "*.ch8"));
		
		root.setTop(createMenuBar());
		root.setBottom(canvas);
		
		stage.show();
	}
	
	private void initializeUnits() {
		List<Register<Integer>> dataRegisters = new ArrayList<>();
		for (int i = 0; i <= 0xF; i++) {
			dataRegisters.add(i, new DataRegister<Integer>());
			dataRegisters.get(i).write(0);
		}
		Register<Integer> programCounter = ProgramCounter.getInstance();
		ALU alu = new ALU(programCounter, dataRegisters.get(0xF), new RandomNumberGenerator());
		
		Memory displayBuffer = BufferFactory.createDisplayBuffer(SCREEN_WIDTH, SCREEN_HEIGHT);
		Memory dirtyBuffer = BufferFactory.createDirtyBuffer();
		gpu = new GPU(displayBuffer, dirtyBuffer, new Screen<Integer>(SCREEN_WIDTH, SCREEN_HEIGHT, PIXEL_SIZE), graphicsContext, drawFlag, clearFlag);
		
		Register<Integer> instructionRegister = InstructionRegister.getInstance();
		Register<Integer> indexRegister = IndexRegister.getInstance();
		cpu = new CPU(new Stack<Integer>(), new RAM(4096), keyboard, dataRegisters, instructionRegister, programCounter, indexRegister, delayTimer, soundTimer, alu, gpu);
	}
	
	private void terminateApplication() {
	    Platform.exit();
	    System.exit(0);
	}
	
	private MenuBar createMenuBar() {
		Menu emulator = new Menu("Interpreter");
		emulator.setOnShowing(event -> paused = true);
		emulator.setOnHidden(event -> paused = false);
		MenuItem open = new MenuItem("Open");
		open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		open.setOnAction(event -> {
			paused = true;
			File file = fileChooser.showOpenDialog(stage);
			if (file != null) {
				gamePath = file.toURI();
				resetGame(gamePath);
			}
			paused = false;
		});
		
		MenuItem exit = new MenuItem("Exit");
		exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
		exit.setOnAction(event -> terminateApplication());
		SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
		emulator.getItems().addAll(open, separatorMenuItem, exit);
		
		Menu options = new Menu("Options");
		options.setOnShowing(event -> paused = true);
		options.setOnHidden(event -> paused = false);
		CheckMenuItem muteSound = new CheckMenuItem("Mute Sound");
		muteSound.setAccelerator(new KeyCodeCombination(KeyCode.F4));
		muteSound.setOnAction(event -> {
			if (muteSound.isSelected()) {
				sound.mute();
			} else {
				sound.unmute();
			}
		});
		MenuItem velocity = new MenuItem("Change velocity");
		velocity.setAccelerator(new KeyCodeCombination(KeyCode.F5));
		velocity.setOnAction(event -> {
			paused = true;
			TextInputDialog dialog = new TextInputDialog(String.valueOf(GAME_VELOCITY));
			dialog.setTitle("Change game velocity");
			dialog.setHeaderText("Game velocity");
			dialog.setContentText("Set game velocity (default 10):");

			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				GAME_VELOCITY = Integer.parseInt(result.get());
			}
			paused = false;
		});
		separatorMenuItem = new SeparatorMenuItem();
		options.getItems().addAll(muteSound, separatorMenuItem, velocity);
		
		Menu game = new Menu("Game");
		game.setOnShowing(event -> paused = true);
		game.setOnHidden(event -> paused = false);
		CheckMenuItem pause = new CheckMenuItem("Pause");
		pause.setAccelerator(new KeyCodeCombination(KeyCode.F2));
		pause.setOnAction(event -> paused = pause.isSelected());
		MenuItem reset = new MenuItem("Reset");
		reset.setAccelerator(new KeyCodeCombination(KeyCode.F3));
		reset.setOnAction(event -> resetGame(gamePath));
		game.getItems().addAll(pause, reset);
		
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(emulator, options, game);
		return menuBar;
	}
	
	private void resetGame(URI gamePath) {
		gpu.clearScreen();
		startGame(gamePath);
	}
	
	private void startGame(URI gamePath) {
		cpu.initialize(0x200, 0x0, 0x0, 0x0, 0x0, fontset);
		loadGame(gamePath);
		if (!running) {
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new InstructionCycle(), 0, 17, TimeUnit.MILLISECONDS);
			running = true;
		}
	}
	
	private void loadGame(URI gamePath) {
		try {
			byte[] ROM = Files.readAllBytes(Paths.get(gamePath));
			cpu.loadROM(ROM, 0x200);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class InstructionCycle implements Runnable {

		@Override
		public void run() {
			if (!paused) {
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
	 			
	 			for (int i = 0; i < GAME_VELOCITY; i++) {
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
