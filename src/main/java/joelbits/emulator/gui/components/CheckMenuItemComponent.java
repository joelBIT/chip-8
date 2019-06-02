package joelbits.emulator.gui.components;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.input.KeyCodeCombination;
import lombok.Builder;

@Builder
public class CheckMenuItemComponent {
    private String displayName;
    private KeyCodeCombination keyCombination;

    public static class CheckMenuItemComponentBuilder {
        public CheckMenuItem build() {
            CheckMenuItem menuItem = new CheckMenuItem(displayName);
            menuItem.setAccelerator(keyCombination);

            return menuItem;
        }
    }
}
