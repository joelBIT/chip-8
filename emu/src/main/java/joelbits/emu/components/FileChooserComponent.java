package joelbits.emu.components;

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
	
	public void removeExtensions(List<FileChooser.ExtensionFilter> extensions) {
		fileChooser.getExtensionFilters().removeAll(extensions);
	}
	
	public File showOpenDialog(Stage stage) {
		return fileChooser.showOpenDialog(stage);
	}
}
