package joelbits.emulator.gui;

import java.io.File;
import java.util.Arrays;

import com.google.inject.Guice;
import com.google.inject.Inject;
import javafx.application.Application;
import javafx.event.Event;
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
import joelbits.emulator.cache.EmulatorCache;
import joelbits.emulator.events.ResetEvent;
import joelbits.emulator.gui.components.ComponentCreator;
import joelbits.emulator.gui.components.FileChooserComponent;
import joelbits.emulator.gui.components.TextInputDialogComponent;
import joelbits.emulator.config.InterpreterConfig;
import joelbits.emulator.input.Input;
import joelbits.emulator.modules.ModuleFactory;
import joelbits.emulator.output.Audio;
import joelbits.emulator.settings.GameSettings;
import joelbits.emulator.utils.Chip8Util;

public class GraphicalUserInterface extends Application {
	private Stage stage;
	private FileChooserComponent fileChooser;
	private TextInputDialogComponent velocityDialog;

	@Inject
    private ComponentCreator componentCreator;
	@Inject
    private Chip8Util chip8Util;
	@Inject
    private Audio sound;
	@Inject
    private InterpreterConfig config;
	@Inject
    private GameSettings settings;
	@Inject
    private Input<Integer, KeyCode> keyboard;

	public static void main(String[] args) { launch(args); }

	@Override
	public void start(Stage stage) throws Exception {
        Guice.createInjector(ModuleFactory.componentModule(), ModuleFactory.soundModule(), ModuleFactory
                .settingsModule(), ModuleFactory.keyboardModule())
                .injectMembers(this);
		this.stage = stage;
		stage.setTitle("Chip-8 interpreter");

		Canvas canvas = new Canvas(config.canvasWidth(), config.canvasHeight());
		GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
		graphicsContext.setFill(Color.WHITE);
		EmulatorCache.setGraphicsContext(graphicsContext);

		velocityDialog = componentCreator
                .inputDialog("Change game velocity", "Game velocity", "Set game velocity (default 10):", String
                        .valueOf(settings.getVelocity()));
		fileChooser = componentCreator.fileChooser();
		
		BorderPane root = new BorderPane();
		root.setStyle("-fx-background: black;");
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setOnCloseRequest(event -> chip8Util.terminateApplication());
		
		scene.setOnKeyPressed(event -> keyboard.press(event.getCode()));
		scene.setOnKeyReleased(event -> keyboard.releasePressed());
		
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
                        .CONTROL_DOWN), event -> chip8Util.terminateApplication());

		return componentCreator.menu("Interpreter", Arrays
                .asList(open, exit), event -> settings.setPaused(true), event -> settings.setPaused(false));
	}
	
	private void openLoadFileDialog() {
		settings.setPaused(true);
		File file = fileChooser.showOpenDialog(stage);
		if (file != null) {
			settings.setGamePath(file.toURI());
			new ResetEvent().handle(new Event(Event.ANY));
		}
		settings.setPaused(false);
	}
	
	private Menu createGameMenu() {
		CheckMenuItem pause = componentCreator.checkMenuItem("Pause", new KeyCodeCombination(KeyCode.F2));
		pause.setOnAction(event -> settings.setPaused(pause.isSelected()));

		MenuItem reset = componentCreator
                .menuItem("Reset", new KeyCodeCombination(KeyCode.F3), event -> new ResetEvent()
                        .handle(new Event(Event.ANY)));
		
		return componentCreator
                .menu("Game", Arrays.asList(pause, reset), event -> settings
                        .setPaused(true), event -> settings.setPaused(false));
	}
	
	private Menu createOptionsMenu() {
		CheckMenuItem muteSound = componentCreator.checkMenuItem("Mute Sound", new KeyCodeCombination(KeyCode.F4));
		muteSound.setOnAction(event -> toggleMute(muteSound));
		
		MenuItem velocity = componentCreator
                .menuItem("Change velocity", new KeyCodeCombination(KeyCode.F5), event -> showVelocityDialog());
		
		return componentCreator
                .menu("Options", Arrays
                        .asList(muteSound, velocity), event -> settings
                        .setPaused(true), event -> settings.setPaused(false));
	}

    private void toggleMute(CheckMenuItem muteSound) {
        if (muteSound.isSelected()) {
            sound.mute();
        } else {
            sound.unmute();
        }
    }
	
	private void showVelocityDialog() {
		settings.setPaused(true);
        velocityDialog.showDialog().ifPresent(s -> settings.setVelocity(Integer.parseInt(s)));
		settings.setPaused(false);
	}
}
