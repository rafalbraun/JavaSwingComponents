package web_browser;

import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

public class EditorPane extends JFrame implements ActionListener, HyperlinkListener {
    private final Color PANEL_COLOR = UIManager.getColor ( "Panel.background" );

    private final JEditorPane editorPane = new JEditorPane();
    private final JTextField input = new JTextField();
    private final JButton launchButton = new JButton("Launch");
    private final JButton prevButton = new JButton("Previous");
    private final JButton nextButton = new JButton("Next");
    private final LinkedList<URL> browsingHistory = new LinkedList<>();
    private ListIterator<URL> currentPage;

    public EditorPane(String startPage) {
        initUI();

        URL url = null;
        try {
            url = new URL(startPage);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HyperlinkEvent event = new HyperlinkEvent(this, HyperlinkEvent.EventType.ACTIVATED, url);
        hyperlinkUpdate(event);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            URL url = new URL(input.getText());
            input.setText(url.toString());
            editorPane.setPage(url);

            //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            browsingHistory.addFirst(url);
            currentPage = browsingHistory.listIterator();
            currentPage.next();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                //Desktop.getDesktop().browse(e.getURL().toURI());
                //System.out.println(e.getURL().toURI());

                URL url = e.getURL();
                input.setText(url.toString());
                editorPane.setPage(url);

                browsingHistory.addFirst(url);
                currentPage = browsingHistory.listIterator();
                currentPage.next();

                //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void initUI() {
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.addHyperlinkListener(this);

        launchButton.addActionListener(this);

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPage.hasNext()) {
                    URL url = currentPage.next();

                    input.setText(url.toString());

                    try {
                        URL newUrl = new URL(url + "?timestamp=" + System.currentTimeMillis());
                        editorPane.setPage(newUrl);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    editorPane.updateUI();

                    System.out.println("loading page: " + url.toString());
                }
            }
        });

        JPanel addressBar = new JPanel(new BorderLayout());
        addressBar.setBackground(PANEL_COLOR);
        addressBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        addressBar.add(input, BorderLayout.CENTER);
        addressBar.add(launchButton, BorderLayout.EAST);
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        buttonsPanel.add(prevButton);
        //buttonsPanel.add(nextButton);
        addressBar.add(buttonsPanel, BorderLayout.WEST);

        getContentPane().setLayout(new BorderLayout());
        add(editorScrollPane, BorderLayout.CENTER);
        add(addressBar, BorderLayout.NORTH);
    }

}
