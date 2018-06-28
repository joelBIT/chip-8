package joelbits.emulator.gui;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import com.google.inject.Guice;
import com.google.inject.Inject;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import joelbits.emulator.Chip8;
import joelbits.emulator.gui.components.ComponentCreator;
import joelbits.emulator.gui.components.FileChooserComponent;
import joelbits.emulator.gui.components.TextInputDialogComponent;
import joelbits.emulator.config.InterpreterConfig;
import joelbits.emulator.modules.ComponentModule;
import joelbits.emulator.settings.GameSettings;

public class GraphicalUserInterface extends Application {
	private Stage stage;
	private FileChooserComponent fileChooser;
	private TextInputDialogComponent velocityDialog;
	private GameSettings settings;
	private Chip8 chip8;
	@Inject
    private ComponentCreator componentCreator;

	public static void main(String[] args) { launch(args); }

	@Override
	public void start(Stage stage) throws Exception {
        Guice.createInjector(new ComponentModule()).injectMembers(this);
		this.stage = stage;
		stage.setTitle("Chip-8 interpreter");
		
		InterpreterConfig config = new InterpreterConfig();
		Canvas canvas = new Canvas(config.canvasWidth(), config.canvasHeight());
		GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
		graphicsContext.setFill(Color.WHITE);
		
		settings = new GameSettings();
		velocityDialog = componentCreator
                .inputDialog("Change game velocity", "Game velocity", "Set game velocity (default 10):", String
                        .valueOf(settings.getVelocity()));
		fileChooser = componentCreator.fileChooser();
		chip8 = new Chip8(settings, graphicsContext);
		
		BorderPane root = new BorderPane();
		root.setStyle("-fx-background: black;");
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setOnCloseRequest(event -> chip8.terminateApplication());
		
		scene.setOnKeyPressed(event -> chip8.keyboard().press(event.getCode()));
		scene.setOnKeyReleased(event -> chip8.keyboard().releasePressed());
		
		root.setTop(componentCreator.menuBar(createInterpreterMenu(), createOptionsMenu(), createGameMenu()));
		root.setBottom(canvas);
		
		stage.show();
	}
	
	private Menu createInterpreterMenu() {
		MenuItem open = componentCreator
                .menuItem("Open", new KeyCodeCombination(KeyCode.O, KeyCombination
                        .CONTROL_DOWN), event -> openLoadFileDialog());
		MenuItem exit = componentCreator
                .menuItem("Exit", new KeyCodeCombination(KeyCode.Q, KeyCombination
                        .CONTROL_DOWN), event -> chip8.terminateApplication());

		return componentCreator.menu("Interpreter", Arrays
                .asList(open, exit), event -> settings.setPaused(true), event -> settings.setPaused(false));
	}
	
	private void openLoadFileDialog() {
		settings.setPaused(true);
		File file = fileChooser.showOpenDialog(stage);
		if (file != null) {
			settings.setGamePath(file.toURI());
			chip8.resetGame();
		}
		settings.setPaused(false);
	}
	
	private Menu createGameMenu() {
		CheckMenuItem pause = new CheckMenuItem("Pause");
		pause.setAccelerator(new KeyCodeCombination(KeyCode.F2));
		pause.setOnAction(event -> settings.setPaused(pause.isSelected()));

		MenuItem reset = componentCreator
                .menuItem("Reset", new KeyCodeCombination(KeyCode.F3), event -> chip8.resetGame());
		
		return componentCreator.menu("Game", Arrays.asList(pause, reset), event -> settings.setPaused(true), event -> settings.setPaused(false));
	}
	
	private Menu createOptionsMenu() {
		CheckMenuItem muteSound = new CheckMenuItem("Mute Sound");
		muteSound.setAccelerator(new KeyCodeCombination(KeyCode.F4));
		muteSound.setOnAction(event -> chip8.toggleMute(muteSound));
		
		MenuItem velocity = componentCreator
                .menuItem("Change velocity", new KeyCodeCombination(KeyCode.F5), event -> showVelocityDialog());
		
		return componentCreator
                .menu("Options", Arrays
                        .asList(muteSound, velocity), event -> settings
                        .setPaused(true), event -> settings.setPaused(false));
	}
	
	private void showVelocityDialog() {
		settings.setPaused(true);
		
		Optional<String> result = velocityDialog.showDialog();
        result.ifPresent(s -> settings.setVelocity(Integer.parseInt(s)));
		
		settings.setPaused(false);
	}
}
