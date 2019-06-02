package joelbits.emulator.gui.components;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import lombok.Builder;

import java.util.List;

@Builder
public class MenuBarComponent {
    private List<Menu> menus;

    public static class MenuBarComponentBuilder {
        public MenuBar build() {
            MenuBar menuBar = new MenuBar();
            menuBar.getMenus().addAll(menus);

            return menuBar;
        }
    }
}
