package joelbits.emulator.gui;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.inject.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
		stage.setOnCloseRequest(event -> {
			Platform.exit();
			System.exit(0);
		});
		
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
		MenuItem open = createMenuItem("Open", event -> openLoadFileDialog(), new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		MenuItem exit = createMenuItem("Exit", event -> {
			Platform.exit();
			System.exit(0);
		}, new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));

		return createMenu(Arrays.asList(open, exit), "Interpreter");
	}

	private MenuItem createMenuItem(String displayName, EventHandler<ActionEvent> event, KeyCodeCombination keyCode) {
		return MenuItemComponent.builder()
				.displayName(displayName)
				.event(event)
				.keyCombination(keyCode)
				.build();
	}

	private Menu createMenu(List<MenuItem> menuItems, String title) {
		return MenuComponent.builder()
				.menuItems(menuItems)
				.title(title)
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
		CheckMenuItem pause = createCheckMenuItem("Pause", new KeyCodeCombination(KeyCode.F2));
		pause.setOnAction(event -> settings.setPaused(pause.isSelected()));
		MenuItem reset = createMenuItem("Reset", event -> new ResetEvent().handle(new Event(Event.ANY)), new KeyCodeCombination(KeyCode.F3));

		return createMenu(Arrays.asList(pause, reset), "Game");
	}

	private CheckMenuItem createCheckMenuItem(String displayName, KeyCodeCombination keyCode) {
		return CheckMenuItemComponent.builder()
				.displayName(displayName)
				.keyCombination(keyCode)
				.build();
	}
	
	private Menu createOptionsMenu() {
		CheckMenuItem muteSound = createCheckMenuItem("Mute Sound", new KeyCodeCombination(KeyCode.F4));
		muteSound.setOnAction(event -> toggleMute(muteSound));
		MenuItem velocity = createMenuItem("Change velocity", event -> showVelocityDialog(), new KeyCodeCombination(KeyCode.F5));

		return createMenu(Arrays.asList(muteSound, velocity), "Options");
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