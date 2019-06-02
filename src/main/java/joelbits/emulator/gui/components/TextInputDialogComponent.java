package joelbits.emulator.gui.components;

import java.util.Optional;

import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import lombok.Builder;

@Builder
public class TextInputDialogComponent extends AbstractDialogComponent<String> {
	private final Dialog<String> dialog;
	private String title;
	private String header;
	private String content;
	private String value;

	public TextInputDialogComponent(Dialog<String> dialog) {
		super(dialog);
		this.dialog = dialog;
	}

	@Override
	public Optional<String> showDialog() {
		return dialog.showAndWait();
	}

	public static class TextInputDialogComponentBuilder {
		public TextInputDialogComponent build() {
			TextInputDialogComponent dialog = new TextInputDialogComponent(new TextInputDialog(value));
			dialog.setTitle(title);
			dialog.setHeaderText(header);
			dialog.setContentText(content);

			return dialog;
		}
	}
}
