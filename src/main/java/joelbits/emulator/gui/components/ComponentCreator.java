package joelbits.emulator.gui.components;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.FileChooser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public TextInputDialogComponent inputDialog(String title, String header, String content, String value) {
        TextInputDialogComponent dialog = new TextInputDialogComponent(new TextInputDialog(value));
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        return dialog;
    }

    public CheckMenuItem checkMenuItem(String displayName, KeyCodeCombination keyCombination) {
        CheckMenuItem menuItem = new CheckMenuItem(displayName);
        menuItem.setAccelerator(keyCombination);

        return menuItem;
    }

    public Menu menu(String title, List<MenuItem> menuItems, EventHandler<Event> onShowing, EventHandler<Event> onHidden) {
        Menu menu = new Menu(title);
        menu.setOnShowing(onShowing);
        menu.setOnHidden(onHidden);
        menu.getItems().addAll(menuItems);

        return menu;
    }
}
