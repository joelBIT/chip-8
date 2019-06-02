package joelbits.emulator.gui.components;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import lombok.Builder;

import java.util.List;

@Builder
public class MenuComponent {
    private String title;
    private List<MenuItem> menuItems;
    private EventHandler<Event> onShowing;
    private EventHandler<Event> onHidden;

    public static class MenuComponentBuilder {
        public Menu build() {
            Menu menu = new Menu(title);
            menu.setOnShowing(onShowing);
            menu.setOnHidden(onHidden);
            menu.getItems().addAll(menuItems);

            return menu;
        }
    }
}
