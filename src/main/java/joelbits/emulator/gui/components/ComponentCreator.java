package joelbits.emulator.gui.components;

import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.FileChooser;

import java.util.ArrayList;
import java.util.Arrays;

public class ComponentCreator {
    public MenuBar menuBar(Menu... menus) {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menus);

        return menuBar;
    }

    public FileChooserComponent fileChooser() {
        FileChooserComponent fileChooser = new FileChooserComponent(new FileChooser());
        fileChooser.addExtensions(new ArrayList<>(Arrays
                .asList(new FileChooser
                        .ExtensionFilter("ch8", "*.ch8"), new FileChooser
                        .ExtensionFilter("rom", "*.rom"))));

        return fileChooser;
    }

    public CheckMenuItem checkMenuItem(String displayName, KeyCodeCombination keyCombination) {
        CheckMenuItem menuItem = new CheckMenuItem(displayName);
        menuItem.setAccelerator(keyCombination);

        return menuItem;
    }
}
