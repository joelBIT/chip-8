package joelbits.emu;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
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
import joelbits.emu.cpu.CPU;

/**
 * Last 8 or 16 bits of each int are used to represent an unsigned byte or an unsigned short respectively. A ROM is written to memory starting
 * at memory location 0x200.
 * 
 */
public class Chip8 extends Application {
	private CPU cpu = new CPU();
	private FileChooser fileChooser = new FileChooser();
	private GraphicsContext graphicsContext;
	private Stage stage;
	private BorderPane root;
	private int GAME_VELOCITY = 10;
	private URI gamePath;
	private boolean running;
	private boolean paused;
	protected final static int fontset[] =
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
		
		root = new BorderPane();
		root.setStyle("-fx-background: black;");
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setOnCloseRequest(event -> terminateApplication());
		
		scene.setOnKeyPressed(event -> getCPU().getKeyboard().pressKey(event.getCode()));
		scene.setOnKeyReleased(event -> getCPU().getKeyboard().releasePressedKey());
		
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("rom", "*.rom"), new FileChooser.ExtensionFilter("ch8", "*.ch8"));
		
		Canvas canvas = new Canvas(896, 448);

		graphicsContext = canvas.getGraphicsContext2D();
		graphicsContext.setFill(Color.WHITE);
		
		root.setTop(createMenuBar());
		root.setBottom(canvas);
		
		stage.show();
	}
	
	private void terminateApplication() {
	    Platform.exit();
	    System.exit(0);
	}
	
	protected CPU getCPU() {
		return cpu;
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
				clearDisplay();
				gamePath = file.toURI();
				startGame(gamePath);
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
				getCPU().getSound().mute();
			} else {
				getCPU().getSound().unmute();
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
		reset.setOnAction(event -> {
			clearDisplay();
			startGame(gamePath);
		});
		game.getItems().addAll(pause, reset);
		
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(emulator, options, game);
		return menuBar;
	}
	
	private void clearDisplay() {
		int displayBufferSize = getCPU().getDisplayBuffer().size();
		for (int i = 0; i < displayBufferSize; i++) {
			int coordinateX = i % getCPU().getScreen().width();
			int coordinateY = i / getCPU().getScreen().width();
			graphicsContext.clearRect(coordinateX*getCPU().getScreen().pixelSize(), coordinateY*getCPU().getScreen().pixelSize(), getCPU().getScreen().pixelSize(), getCPU().getScreen().pixelSize());
		}
	}
	
	private void startGame(URI gamePath) {
		getCPU().initialize(0x200, 0x0, 0x0, 0x0, 0x0, fontset);
		loadGame(gamePath);
		if (!running) {
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new InstructionCycle(), 0, 17, TimeUnit.MILLISECONDS);
			running = true;
		}
	}
	
	private void loadGame(URI gamePath) {
		try {
			byte[] ROM = Files.readAllBytes(Paths.get(gamePath));
			getCPU().loadROM(ROM, 0x200);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class InstructionCycle implements Runnable {

		@Override
		public void run() {
			if (!paused) {
	 			if (getCPU().readDelayTimer() > 0) {
	 				getCPU().decrementDelayTimer();
	 			}
	 			
	 			if (getCPU().readSoundTimer() > 0) {
	 				getCPU().getSound().start();
	 				getCPU().decrementSoundTimer();
	 			}
	 			
	 			for (int i = 0; i < GAME_VELOCITY; i++) {
	 				if (getCPU().isClearFlag()) {
	 	 				clearDisplay();
	 	 				getCPU().toggleClearFlag();
	 	 			}
	 	 			
	 	 			if (getCPU().isDrawFlag()) {
	 	 				drawSprites();
	 	 				getCPU().toggleDrawFlag();
	 	 			}
	 				
	 				getCPU().nextInstructionCycle();
	 			}
			}
			return;
		}
		
		private synchronized void drawSprites() {
			int dirtyBufferSize = getCPU().getDirtyBuffer().size();
			for (int i = 0; i < dirtyBufferSize; i++) {
				int dirtyLocation = getCPU().getDirtyBuffer().read(i);
				int coordinateX = dirtyLocation % getCPU().getScreen().width();
				int coordinateY = dirtyLocation / getCPU().getScreen().width();
				if (getCPU().getDisplayBuffer().read(dirtyLocation) != 0) {
					graphicsContext.fillRect(coordinateX*getCPU().getScreen().pixelSize(), coordinateY*getCPU().getScreen().pixelSize(), getCPU().getScreen().pixelSize(), getCPU().getScreen().pixelSize());
				} else {
					graphicsContext.clearRect(coordinateX*getCPU().getScreen().pixelSize(), coordinateY*getCPU().getScreen().pixelSize(), getCPU().getScreen().pixelSize(), getCPU().getScreen().pixelSize());
				}
			}
		}
	}
}
