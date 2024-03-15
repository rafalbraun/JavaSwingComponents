package web_browser;

import javax.swing.*;
import java.awt.*;

public class WebBrowser {
    private static final String START_PAGE = "https://news.ycombinator.com/";

    public static void main(String[] args) {
        // setting user agent, sometimes needed
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36");

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new EditorPane(START_PAGE);
        frame.setVisible(true);
        frame.setSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
