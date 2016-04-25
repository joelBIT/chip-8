package joelbits.emu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import joelbits.emu.cpu.CPU;
import joelbits.emu.output.Display;

/**
 * Chip-8 emulator.
 * 
 * Last 8 or 16 bits of each int are used to represent an unsigned byte or an unsigned short respectively. A ROM is written to memory starting
 * at memory location 0x200.
 * 
 * @author rollnyj
 * 
 */
public class Chip8 extends Application {
	private final CPU cpu = new CPU();
	private Canvas canvas;
	private GraphicsContext graphicsContext;
	private final int PIXEL_SIZE = 14;
	private final static int fontset[] =
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
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Chip-8 emulator");
		
		Group root = new Group();
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		
		primaryStage.setOnCloseRequest(event -> {
		    Platform.exit();
		    System.exit(0);
		});
		
		scene.setOnKeyPressed(event -> getCPU().getKeyboard().keyPressed(event.getCode()));
		scene.setOnKeyReleased(event -> getCPU().getKeyboard().keyReleased());
		
		canvas = new Canvas(896, 448);
		root.getChildren().add(canvas);
		graphicsContext = canvas.getGraphicsContext2D();
		
		primaryStage.show();
		
		getCPU().initialize(0x200, 0x0, 0x0, 0x0, 0x0, fontset);
		loadGame("PONG");
		
		for (int i = 0; i < 1000; i++) {
			getCPU().nextInstructionCycle();
			if (getCPU().isDrawFlag()) {
				drawSprites();
				getCPU().toggleDrawFlag();
			}
		}
	}
	
	private void drawSprites() {
		int displayBufferSize = Display.SCREEN_WIDTH * Display.SCREEN_HEIGHT;
		for (int i = 0; i < displayBufferSize; i++) {
			int coordinateX = i % Display.SCREEN_WIDTH;
			int coordinateY = i / Display.SCREEN_WIDTH;
			if (getCPU().getDisplay().readFromDisplayBuffer(coordinateX, coordinateY) != 0) {
				graphicsContext.fillRect(coordinateX*PIXEL_SIZE, coordinateY*PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
			}
		}
	}
	
	private CPU getCPU() {
		return cpu;
	}
	
	private void loadGame(String game) {
		try {
			byte[] ROM = Files.readAllBytes(Paths.get(getClass().getResource(game).toString().substring(8)));
			getCPU().loadROM(ROM, 0x200);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
