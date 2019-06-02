package joelbits.emulator.gui.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCodeCombination;
import lombok.Builder;

@Builder
public class MenuItemComponent {
    private String displayName;
    private KeyCodeCombination keyCombination;
    private EventHandler<ActionEvent> event;

    public static class MenuItemComponentBuilder {
        public MenuItem build() {
            MenuItem menuItem = new MenuItem(displayName);
            menuItem.setAccelerator(keyCombination);
            menuItem.setOnAction(event);

            return menuItem;
        }
    }
}
