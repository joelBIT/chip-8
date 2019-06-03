package joelbits.emulator.gui;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import com.google.inject.*;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import joelbits.emulator.cache.EmulatorCache;
import joelbits.emulator.events.ResetEvent;
import joelbits.emulator.gui.components.*;
import joelbits.emulator.config.InterpreterConfig;
import joelbits.emulator.input.Input;
import joelbits.emulator.modules.InterpreterModule;
import joelbits.emulator.output.Audio;
import joelbits.emulator.settings.GameSettings;
import joelbits.emulator.utils.Chip8Util;

public class GraphicalUserInterface extends Application {
	private Stage stage;
	private FileChooserComponent fileChooser;
	private TextInputDialogComponent velocityDialog;

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
	public void start(Stage stage) {
		handleInjection();

		this.stage = stage;
		stage.setTitle("Chip-8 interpreter");

		velocityDialog = createVelocityDialog();
		fileChooser = createFileChooser();
		
		BorderPane root = new BorderPane();
		root.setStyle("-fx-background: black;");
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setOnCloseRequest(event -> chip8Util.terminateApplication());
		
		scene.setOnKeyPressed(event -> keyboard.press(event.getCode()));
		scene.setOnKeyReleased(event -> keyboard.releasePressed());
		
		root.setTop(MenuBarComponent.builder()
				.menus(Arrays.asList(createInterpreterMenu(), createOptionsMenu(), createGameMenu()))
				.build());
		root.setBottom(createCanvas());
		
		stage.show();
	}

	private FileChooserComponent createFileChooser() {
		return FileChooserComponent.builder()
				.fileChooser(new FileChooser())
				.extensions(Arrays.asList(new FileChooser
						.ExtensionFilter("ch8", "*.ch8"), new FileChooser
						.ExtensionFilter("rom", "*.rom")))
				.build();
	}

	private TextInputDialogComponent createVelocityDialog() {
		return TextInputDialogComponent.builder()
				.title("Change game velocity")
				.header("Game velocity")
				.content("Set game velocity (default 10):")
				.value(String.valueOf(settings.getVelocity()))
				.build();
	}

	private void handleInjection() {
		Injector injector = Guice.createInjector(new InterpreterModule());
		injector.injectMembers(this);
		EmulatorCache.getInstance().setInjector(injector);
	}

	private Canvas createCanvas() {
		Canvas canvas = new Canvas(config.canvasWidth(), config.canvasHeight());
		configureGraphic(canvas.getGraphicsContext2D());
		return canvas;
	}

	private void configureGraphic(GraphicsContext graphicsContext) {
		graphicsContext.setFill(Color.WHITE);
		EmulatorCache.getInstance().setGraphicsContext(graphicsContext);
	}

	private Menu createInterpreterMenu() {
		MenuItem open = MenuItemComponent.builder()
				.displayName("Open")
				.event(event -> openLoadFileDialog())
				.keyCombination(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN))
				.build();

		MenuItem exit = MenuItemComponent.builder()
				.displayName("Exit")
				.event(event -> chip8Util.terminateApplication())
				.keyCombination(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN))
				.build();

		return MenuComponent.builder()
				.menuItems(Arrays.asList(open, exit))
				.title("Interpreter")
				.onHidden(event -> settings.setPaused(false))
				.onShowing(event -> settings.setPaused(true))
				.build();
	}
	
	private void openLoadFileDialog() {
		settings.setPaused(true);
		File file = fileChooser.showOpenDialog(stage);
		if (Objects.nonNull(file)) {
			settings.setGamePath(file.toURI());
			new ResetEvent().handle(new Event(Event.ANY));
		}
		settings.setPaused(false);
	}
	
	private Menu createGameMenu() {
		CheckMenuItem pause = CheckMenuItemComponent.builder()
				.displayName("Pause")
				.keyCombination(new KeyCodeCombination(KeyCode.F2))
				.build();
		pause.setOnAction(event -> settings.setPaused(pause.isSelected()));

		MenuItem reset = MenuItemComponent.builder()
				.displayName("Reset")
				.event(event -> new ResetEvent().handle(new Event(Event.ANY)))
				.keyCombination(new KeyCodeCombination(KeyCode.F3))
				.build();

		return MenuComponent.builder()
				.menuItems(Arrays.asList(pause, reset))
				.title("Game")
				.onHidden(event -> settings.setPaused(false))
				.onShowing(event -> settings.setPaused(true))
				.build();
	}
	
	private Menu createOptionsMenu() {
		CheckMenuItem muteSound = CheckMenuItemComponent.builder()
				.displayName("Mute Sound")
				.keyCombination(new KeyCodeCombination(KeyCode.F4))
				.build();
		muteSound.setOnAction(event -> toggleMute(muteSound));

		MenuItem velocity = MenuItemComponent.builder()
				.displayName("Change velocity")
				.event(event -> showVelocityDialog())
				.keyCombination(new KeyCodeCombination(KeyCode.F5))
				.build();

		return MenuComponent.builder()
				.menuItems(Arrays.asList(muteSound, velocity))
				.title("Options")
				.onHidden(event -> settings.setPaused(false))
				.onShowing(event -> settings.setPaused(true))
				.build();
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
