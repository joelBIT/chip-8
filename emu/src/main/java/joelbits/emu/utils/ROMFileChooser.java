package joelbits.emu.utils;

import java.io.File;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ROMFileChooser {
	private final FileChooser fileChooser = new FileChooser();

	public ROMFileChooser() {
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("rom", "*.rom"), new FileChooser.ExtensionFilter("ch8", "*.ch8"));
	}
	
	public File showOpenDialog(Stage stage) {
		return fileChooser.showOpenDialog(stage);
	}
}
