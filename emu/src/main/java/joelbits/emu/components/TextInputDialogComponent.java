package joelbits.emu.components;

import java.util.Optional;

import javafx.scene.control.TextInputDialog;
import joelbits.emu.GameSettings;

public class TextInputDialogComponent {
	private final GameSettings settings;
	private final TextInputDialog dialog;

	public TextInputDialogComponent(GameSettings settings) {
		this.settings = settings;
		dialog = createVelocityDialog();
	}
	
	public void changeGameVelocity() {
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			settings.setVelocity(Integer.parseInt(result.get()));
		}
	}
	
	private TextInputDialog createVelocityDialog() {
		TextInputDialog dialog = new TextInputDialog(String.valueOf(settings.getVelocity()));
		dialog.setTitle("Change game velocity");
		dialog.setHeaderText("Game velocity");
		dialog.setContentText("Set game velocity (default 10):");
		return dialog;
	}
}
