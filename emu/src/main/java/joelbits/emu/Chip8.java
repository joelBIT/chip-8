package joelbits.emu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import joelbits.emu.cpu.CPU;

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
	private static int fontset[] =
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
		Chip8 chip8 = new Chip8();
		
		chip8.getCPU().initialize(0x200, 0x0, 0x0, 0x0, 0x0, fontset);
		chip8.loadGame("pong");
		
		launch(args);
		
		for (;;) {
			chip8.getCPU().nextInstructionCycle();
		}
	}
	
	private CPU getCPU() {
		return cpu;
	}
	
	private void loadGame(String game) {
		try {
			byte[] ROM = Files.readAllBytes(Paths.get(game));
			getCPU().loadROM(ROM, 0x200);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		
		Canvas canvas = new Canvas(600, 400);
		root.getChildren().add(canvas);
	    
		primaryStage.show();
	}
}
