package joelbits.emu;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import joelbits.emu.components.FileChooserComponent;
import joelbits.emu.components.TextInputDialogComponent;

public class GraphicalUserInterface extends Application {
	private GraphicsContext graphicsContext;
	private Stage stage;
	private FileChooserComponent fileChooser = createFileChooser();
	private TextInputDialogComponent velocityDialog;
	private GameSettings settings;
	private Chip8 chip8;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		stage.setTitle("Chip-8 interpreter");
		
		InterpreterConfig config = new InterpreterConfig();
		Canvas canvas = new Canvas(config.canvasWidth(), config.canvasHeight());
		graphicsContext = canvas.getGraphicsContext2D();
		graphicsContext.setFill(Color.WHITE);
		
		settings = new GameSettings();
		velocityDialog = createVelocityDialog();
		chip8 = new Chip8(settings, graphicsContext);
		
		BorderPane root = new BorderPane();
		root.setStyle("-fx-background: black;");
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setOnCloseRequest(event -> chip8.terminateApplication());
		
		scene.setOnKeyPressed(event -> chip8.keyboard().press(event.getCode()));
		scene.setOnKeyReleased(event -> chip8.keyboard().releasePressed());
		
		root.setTop(createMenuBar());
		root.setBottom(canvas);
		
		stage.show();
	}
	
	private FileChooserComponent createFileChooser() {
		FileChooserComponent fileChooser = new FileChooserComponent(new FileChooser());
		fileChooser.addExtensions(new ArrayList<>(Arrays.asList(new FileChooser.ExtensionFilter("ch8", "*.ch8"), new FileChooser.ExtensionFilter("rom", "*.rom"))));
		
		return fileChooser;
	}

	private TextInputDialogComponent createVelocityDialog() {
		TextInputDialogComponent velocityDialog = new TextInputDialogComponent(new TextInputDialog(String.valueOf(settings.getVelocity())));
		velocityDialog.setTitle("Change game velocity");
		velocityDialog.setHeaderText("Game velocity");
		velocityDialog.setContentText("Set game velocity (default 10):");
		
		return velocityDialog;
	}
	
	private MenuBar createMenuBar() {
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(createInterpreterMenu(), createOptionsMenu(), createGameMenu());
		
		return menuBar;
	}
	
	private Menu createInterpreterMenu() {
		MenuItem open = createMenuItem("Open", new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN), event -> openLoadFileDialog());
		MenuItem exit = createMenuItem("Exit", new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN), event -> chip8.terminateApplication());

		return createMenu("Interpreter", Arrays.asList(open, exit));
	}
	
	private MenuItem createMenuItem(String displayName, KeyCodeCombination keyCombination, EventHandler<ActionEvent> event) {
		MenuItem menuItem = new MenuItem(displayName);
		menuItem.setAccelerator(keyCombination);
		menuItem.setOnAction(event);
		
		return menuItem;
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
	
	private Menu createMenu(String title, List<MenuItem> menuItems) {
		Menu menu = new Menu(title);
		menu.setOnShowing(event -> settings.setPaused(true));
		menu.setOnHidden(event -> settings.setPaused(false));
		menu.getItems().addAll(menuItems);
		
		return menu;
	}
	
	private Menu createGameMenu() {
		CheckMenuItem pause = new CheckMenuItem("Pause");
		pause.setAccelerator(new KeyCodeCombination(KeyCode.F2));
		pause.setOnAction(event -> settings.setPaused(pause.isSelected()));
		
		MenuItem reset = createMenuItem("Reset", new KeyCodeCombination(KeyCode.F3), event -> chip8.resetGame());
		
		return createMenu("Game", Arrays.asList(pause, reset));
	}
	
	private Menu createOptionsMenu() {
		CheckMenuItem muteSound = new CheckMenuItem("Mute Sound");
		muteSound.setAccelerator(new KeyCodeCombination(KeyCode.F4));
		muteSound.setOnAction(event -> chip8.toggleMute(muteSound));
		
		MenuItem velocity = createMenuItem("Change velocity", new KeyCodeCombination(KeyCode.F5), event -> showVelocityDialog());
		
		return createMenu("Options", Arrays.asList(muteSound, velocity));
	}
	
	private void showVelocityDialog() {
		settings.setPaused(true);
		
		Optional<String> result = velocityDialog.showDialog();
		if (result.isPresent()) {
			settings.setVelocity(Integer.parseInt(result.get()));
		}
		
		settings.setPaused(false);
	}
}
