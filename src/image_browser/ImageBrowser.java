package image_browser;

import javax.swing.*;

public class ImageBrowser {
    private static final String IMAGES_FOLDER = "resources/images/resized";

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {

        ImagePanel imagePanel = new ImagePanel(IMAGES_FOLDER);
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        int unitIncrement = 20;
        int blockIncrement = 100;
        verticalScrollBar.setUnitIncrement(unitIncrement);
        verticalScrollBar.setBlockIncrement(blockIncrement);

        JFrame frame = new JFrame("Image Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.getContentPane().add(scrollPane);
        frame.setVisible(true);

        imagePanel.loadImagesAsync();

    }

}
