package joelbits.emulator.gui.components;

import java.io.File;
import java.util.List;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileChooserComponent {
	private final FileChooser fileChooser;
	
	public FileChooserComponent(FileChooser fileChooser) {
		this.fileChooser = fileChooser;
	}
	
	public void addExtensions(List<FileChooser.ExtensionFilter> extensions) {
		fileChooser.getExtensionFilters().addAll(extensions);
	}
	
	public File showOpenDialog(Stage stage) {
		return fileChooser.showOpenDialog(stage);
	}
}
