package joelbits.emulator.gui.components;

import java.util.Optional;

import javafx.scene.control.Dialog;

public abstract class AbstractDialogComponent<T> {
	private final Dialog<T> dialog;
	
	public AbstractDialogComponent(Dialog<T> dialog) {
		this.dialog = dialog;
	}

	public void setTitle(String title) {
		dialog.setTitle(title);
	}
	
	public void setHeaderText(String headerText) {
		dialog.setHeaderText(headerText);
	}
	
	public void setContentText(String contentText) {
		dialog.setContentText(contentText);
	}
	
	public abstract Optional<T> showDialog();
}
