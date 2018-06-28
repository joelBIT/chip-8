package joelbits.emulator.gui.components;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

public class ComponentCreator {

    public MenuBar menuBar(Menu... menus) {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menus);

        return menuBar;
    }
}
