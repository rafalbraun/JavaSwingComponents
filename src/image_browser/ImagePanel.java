package image_browser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImagePanel extends JPanel {
    private static final int ICON_SIZE = 100;
    private static final int THICKNESS = 12;
    private final String directoryPath;
    private final Color color = UIManager.getColor ( "Panel.background" );
    private JPanel selectedLabel;

    public Map<String, JLabel> imagesData = new HashMap<>();

    public ImagePanel(String directoryPath) {
        this.directoryPath = directoryPath;
        setLayout(new WrapLayout(FlowLayout.LEFT));
        loadFiles();
    }

    private void loadFiles() {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file)) {
                    try {
                        Image image = ImageIO.read(new File("resources/images/empty.png"));
                        JLabel iconLabel = new JLabel(new ImageIcon(image));
                        JLabel textLabel = new JLabel("<html><center>" + getFileExtension(file) + "</center></html>");
                        textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                        JPanel panel = new JPanel(new BorderLayout());
                        panel.add(iconLabel, BorderLayout.CENTER);
                        panel.add(textLabel, BorderLayout.SOUTH);
                        panel.setBorder(BorderFactory.createLineBorder(color, THICKNESS));

                        panel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if (selectedLabel != null) {
                                    selectedLabel.setBorder(BorderFactory.createLineBorder(color, THICKNESS));
                                }
                                panel.setBorder(BorderFactory.createLineBorder(Color.BLUE, THICKNESS));
                                selectedLabel = panel;
                            }
                        });

                        add(panel);

                        imagesData.put(file.getAbsolutePath(), iconLabel);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".bmp");
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOfDot = name.lastIndexOf('.');
        if (lastIndexOfDot != -1) {
            return name.substring(lastIndexOfDot + 1);
        }
        return "";
    }

    public void loadImagesAsync() {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (Map.Entry<String, JLabel> entry : imagesData.entrySet()) {
            executorService.submit(() -> {
                try {
                    Image image = ImageIO.read(new File(entry.getKey()));
                    //ImageIcon icon = new ImageIcon(image.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));
                    ImageIcon icon = new ImageIcon(image);
                    entry.getValue().setIcon(icon);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
    }

}