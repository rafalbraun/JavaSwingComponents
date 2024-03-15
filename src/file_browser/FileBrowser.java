package file_browser;

import javax.swing.*;
import java.awt.*;

public class FileBrowser {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {

        //Add content to the window.
        String homeDir = System.getProperty("user.home");
        FileManagerLazy fileManager = new FileManagerLazy(homeDir);

        //Create and set up the window.
        JFrame frame = new JFrame("FileBrowser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(fileManager);
        frame.setPreferredSize(new Dimension(960, 980));
        frame.pack();
        frame.setVisible(true);
    }


}
