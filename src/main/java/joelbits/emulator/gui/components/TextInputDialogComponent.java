package joelbits.emulator.gui.components;

import java.util.Optional;

import javafx.scene.control.Dialog;

public class TextInputDialogComponent extends AbstractDialogComponent<String> {
	private final Dialog<String> dialog;

	public TextInputDialogComponent(Dialog<String> dialog) {
		super(dialog);
		this.dialog = dialog;
	}

	@Override
	public Optional<String> showDialog() {
		return dialog.showAndWait();
	}
}
