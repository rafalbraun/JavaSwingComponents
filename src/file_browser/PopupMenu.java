package file_browser;

import javax.swing.*;

public class PopupMenu extends JPopupMenu {
    JMenuItem cutItem, copyItem, pasteItem, deleteItem;

    public PopupMenu(String data) {
        cutItem = new JMenuItem("Cut");
        add(cutItem);
        copyItem = new JMenuItem("Copy");
        add(copyItem);
        pasteItem = new JMenuItem("Paste");
        add(pasteItem);
        deleteItem = new JMenuItem("Delete");
        add(deleteItem);
    }

}
