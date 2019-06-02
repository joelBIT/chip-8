package joelbits.emulator.gui.components;

import java.io.File;
import java.util.List;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Builder;

@Builder
public class FileChooserComponent {
	private final FileChooser fileChooser;
	private List<FileChooser.ExtensionFilter> extensions;
	
	public File showOpenDialog(Stage stage) {
		return fileChooser.showOpenDialog(stage);
	}

	public static class FileChooserComponentBuilder {
		public FileChooserComponent build() {
			fileChooser.getExtensionFilters().addAll(extensions);

			return new FileChooserComponent(fileChooser, extensions);
		}
	}
}